package src.pas.pokemon.agents;


// SYSTEM IMPORTS

import net.sourceforge.argparse4j.inf.Namespace;

import edu.bu.pas.pokemon.agents.NeuralQAgent;
import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Move.Category;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.linalg.Matrix;
import edu.bu.pas.pokemon.nn.Model;
import edu.bu.pas.pokemon.nn.models.Sequential;
import edu.bu.pas.pokemon.nn.layers.Dense;
import edu.bu.pas.pokemon.nn.layers.ReLU;
import edu.bu.pas.pokemon.nn.layers.Tanh;
import edu.bu.pas.pokemon.nn.layers.Sigmoid;

import java.util.Random;
import java.util.List;


// JAVA PROJECT IMPORTS
import src.pas.pokemon.senses.CustomSensorArray;


public class PolicyAgent
        extends NeuralQAgent {
    private double epsilon;
    private static final double EPSILON_START = 0.9;
    private static final double EPSILON_END = 0.05;
    private static final double EPSILON_DECAY = 0.9995;

    private Random random;
    private int gamesPlayed;
    private boolean trainingMode;

    public PolicyAgent() {
        super();
        this.epsilon = EPSILON_START;
        this.random = new Random();
        this.gamesPlayed = 0;
        this.trainingMode = true;
    }

    @Override
    public void train() {
        super.train();
        this.trainingMode = true;
    }

    @Override
    public void eval() {
        super.eval();
        this.trainingMode = false;
    }

    public void initializeSenses(Namespace args) {
        SensorArray modelSenses = new CustomSensorArray();
        this.setSensorArray(modelSenses);
    }

    @Override
    public void initialize(Namespace args) {
        super.initialize(args);
        this.initializeSenses(args);
    }

    @Override
    public Model initModel() {
        Sequential qFunction = new Sequential();

        qFunction.add(new Dense(64, 128));
        qFunction.add(new ReLU());

        qFunction.add(new Dense(128, 128));
        qFunction.add(new ReLU());

        qFunction.add(new Dense(128, 64));
        qFunction.add(new ReLU());

        qFunction.add(new Dense(64, 1));

        return qFunction;
    }

    @Override
    public Integer chooseNextPokemon(BattleView view) {
        TeamView myTeam = this.getMyTeamView(view);
        TeamView oppTeam = this.getOpponentTeamView(view);
        PokemonView oppActive = oppTeam.getActivePokemonView();

        int bestIdx = -1;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int idx = 0; idx < myTeam.size(); idx++) {
            PokemonView myPokemon = myTeam.getPokemonView(idx);

            if (myPokemon.hasFainted()) {
                continue;
            }

            if (idx == myTeam.getActivePokemonIdx() && !myTeam.getActivePokemonView().hasFainted()) {
                continue;
            }

            double score = evaluateSwitchOption(myPokemon, oppActive);

            if (score > bestScore) {
                bestScore = score;
                bestIdx = idx;
            }
        }

        if (bestIdx == -1) {
            for (int idx = 0; idx < myTeam.size(); idx++) {
                if (!myTeam.getPokemonView(idx).hasFainted()) {
                    return idx;
                }
            }
        }

        return bestIdx;
    }

    private double evaluateSwitchOption(PokemonView myPokemon, PokemonView oppPokemon) {
        double score = 0.0;

        double hpRatio = (double) myPokemon.getCurrentStat(Stat.HP) /
                (double) myPokemon.getInitialStat(Stat.HP);
        score += 50.0 * hpRatio;

        Type oppType1 = oppPokemon.getCurrentType1();
        Type oppType2 = oppPokemon.getCurrentType2();
        Type myType1 = myPokemon.getCurrentType1();
        Type myType2 = myPokemon.getCurrentType2();

        double defensiveMatchup = Type.getEffectivenessModifier(oppType1, myType1);
        if (myType2 != null) {
            defensiveMatchup *= Type.getEffectivenessModifier(oppType1, myType2);
        }
        if (oppType2 != null) {
            double matchup2 = Type.getEffectivenessModifier(oppType2, myType1);
            if (myType2 != null) {
                matchup2 *= Type.getEffectivenessModifier(oppType2, myType2);
            }
            defensiveMatchup = Math.max(defensiveMatchup, matchup2);
        }

        if (defensiveMatchup <= 0.5) {
            score += 30.0;
        } else if (defensiveMatchup == 0.0) {
            score += 50.0;
        } else if (defensiveMatchup >= 2.0) {
            score -= 30.0;
        }

        double offensiveMatchup = Type.getEffectivenessModifier(myType1, oppType1);
        if (oppType2 != null) {
            offensiveMatchup *= Type.getEffectivenessModifier(myType1, oppType2);
        }
        if (myType2 != null) {
            double matchup2 = Type.getEffectivenessModifier(myType2, oppType1);
            if (oppType2 != null) {
                matchup2 *= Type.getEffectivenessModifier(myType2, oppType2);
            }
            offensiveMatchup = Math.max(offensiveMatchup, matchup2);
        }

        if (offensiveMatchup >= 2.0) {
            score += 20.0;
        } else if (offensiveMatchup <= 0.5) {
            score -= 10.0;
        }

        if (myPokemon.getCurrentStat(Stat.SPD) > oppPokemon.getCurrentStat(Stat.SPD)) {
            score += 10.0;
        }

        if (myPokemon.getNonVolatileStatus() == edu.bu.pas.pokemon.core.enums.NonVolatileStatus.NONE) {
            score += 10.0;
        }

        return score;
    }

    @Override
    public MoveView getMove(BattleView view) {
        if (this.trainingMode) {
            if (random.nextDouble() < epsilon) {
                List<MoveView> availableMoves = this.getMyTeamView(view).getActivePokemonView().getAvailableMoves();
                if (availableMoves.size() > 0) {
                    return availableMoves.get(random.nextInt(availableMoves.size()));
                }
            }
        }
        return this.argmax(view);
    }

    @Override
    public void afterGameEnds(BattleView view) {
        if (this.trainingMode) {
            gamesPlayed++;

            epsilon = EPSILON_END + (EPSILON_START - EPSILON_END) *
                    Math.exp(-gamesPlayed / 1000.0);

            epsilon = Math.max(EPSILON_END, Math.min(EPSILON_START, epsilon));
        }
    }

}