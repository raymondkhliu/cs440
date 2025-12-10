package src.pas.pokemon.rewards;


// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS

import edu.bu.pas.pokemon.agents.rewards.RewardFunction;
import edu.bu.pas.pokemon.agents.rewards.RewardFunction.RewardType;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Stat;


public class CustomRewardFunction
        extends RewardFunction {

    public CustomRewardFunction() {
        super(RewardType.STATE_ACTION_STATE);
    }

    public double getLowerBound() {
        return -1000.0;
    }

    public double getUpperBound() {
        return 1000.0;
    }

    public double getStateReward(final BattleView state) {
        return 0.0;
    }

    public double getStateActionReward(final BattleView state,
                                       final MoveView action) {
        return 0.0;
    }

    public double getStateActionStateReward(final BattleView state,
                                            final MoveView action,
                                            final BattleView nextState) {

        TeamView myTeam = state.getTeam1View();
        TeamView oppTeam = state.getTeam2View();
        TeamView myTeamNext = nextState.getTeam1View();
        TeamView oppTeamNext = nextState.getTeam2View();

        double reward = 0.0;

        if (nextState.isOver()) {
            boolean iWon = oppTeamNext.getActivePokemonView().hasFainted() ||
                    isTeamDefeated(oppTeamNext);
            boolean iLost = myTeamNext.getActivePokemonView().hasFainted() &&
                    isTeamDefeated(myTeamNext);

            if (iWon) {
                reward += 1000.0;
            }
            if (iLost) {
                reward -= 1000.0;
            }

            return reward;
        }

        PokemonView oppBefore = oppTeam.getActivePokemonView();
        PokemonView oppAfter = oppTeamNext.getActivePokemonView();

        if (oppBefore.getDexIdx() == oppAfter.getDexIdx()) {
            int oppHPBefore = oppBefore.getCurrentStat(Stat.HP);
            int oppHPAfter = oppAfter.getCurrentStat(Stat.HP);
            int damageDealt = oppHPBefore - oppHPAfter;

            if (damageDealt > 0) {
                double damageRatio = (double) damageDealt / (double) oppBefore.getInitialStat(Stat.HP);
                reward += 50.0 * damageRatio;

                if (oppAfter.hasFainted()) {
                    reward += 100.0;
                }
            }
        } else {
            if (oppBefore.hasFainted()) {
                reward += 100.0;
            }
        }

        PokemonView myBefore = myTeam.getActivePokemonView();
        PokemonView myAfter = myTeamNext.getActivePokemonView();

        if (myBefore.getDexIdx() == myAfter.getDexIdx()) {
            int myHPBefore = myBefore.getCurrentStat(Stat.HP);
            int myHPAfter = myAfter.getCurrentStat(Stat.HP);
            int damageTaken = myHPBefore - myHPAfter;

            if (damageTaken > 0) {
                double damageRatio = (double) damageTaken / (double) myBefore.getInitialStat(Stat.HP);
                reward -= 30.0 * damageRatio;
                if (myAfter.hasFainted()) {
                    reward -= 80.0;
                }
            }
        } else {
            if (myBefore.hasFainted()) {
                reward -= 80.0;
            } else {
                reward -= 2.0;
            }
        }

        double myTeamHPBefore = getTeamHPRatio(myTeam);
        double oppTeamHPBefore = getTeamHPRatio(oppTeam);
        double myTeamHPAfter = getTeamHPRatio(myTeamNext);
        double oppTeamHPAfter = getTeamHPRatio(oppTeamNext);

        double hpAdvantageChange = (myTeamHPAfter - oppTeamHPAfter) - (myTeamHPBefore - oppTeamHPBefore);
        reward += 10.0 * hpAdvantageChange;

        int myAliveBefore = countAlivePokemon(myTeam);
        int oppAliveBefore = countAlivePokemon(oppTeam);
        int myAliveAfter = countAlivePokemon(myTeamNext);
        int oppAliveAfter = countAlivePokemon(oppTeamNext);

        if (oppAliveAfter < oppAliveBefore) {
            reward += 50.0;
        }
        if (myAliveAfter < myAliveBefore) {
            reward -= 50.0;
        }

        if (!oppBefore.getNonVolatileStatus().equals(oppAfter.getNonVolatileStatus()) &&
                oppAfter.getNonVolatileStatus() != edu.bu.pas.pokemon.core.enums.NonVolatileStatus.NONE) {
            reward += 5.0;
        }

        if (!myBefore.getNonVolatileStatus().equals(myAfter.getNonVolatileStatus()) &&
                myAfter.getNonVolatileStatus() != edu.bu.pas.pokemon.core.enums.NonVolatileStatus.NONE) {
            reward -= 5.0;
        }

        reward = Math.max(getLowerBound(), Math.min(getUpperBound(), reward));

        return reward;
    }

    private double getTeamHPRatio(TeamView team) {
        double totalCurrent = 0.0;
        double totalMax = 0.0;

        for (int i = 0; i < team.size(); i++) {
            PokemonView p = team.getPokemonView(i);
            totalCurrent += p.getCurrentStat(Stat.HP);
            totalMax += p.getInitialStat(Stat.HP);
        }

        return totalMax > 0 ? totalCurrent / totalMax : 0.0;
    }

    private int countAlivePokemon(TeamView team) {
        int count = 0;
        for (int i = 0; i < team.size(); i++) {
            if (!team.getPokemonView(i).hasFainted()) {
                count++;
            }
        }
        return count;
    }

    private boolean isTeamDefeated(TeamView team) {
        for (int i = 0; i < team.size(); i++) {
            if (!team.getPokemonView(i).hasFainted()) {
                return false;
            }
        }
        return true;
    }

}
