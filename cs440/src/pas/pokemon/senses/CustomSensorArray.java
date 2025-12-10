package src.pas.pokemon.senses;


// SYSTEM IMPORTS


// JAVA PROJECT IMPORTS

import edu.bu.pas.pokemon.agents.senses.SensorArray;
import edu.bu.pas.pokemon.core.Battle.BattleView;
import edu.bu.pas.pokemon.core.Move.MoveView;
import edu.bu.pas.pokemon.core.Move.Category;
import edu.bu.pas.pokemon.core.Pokemon.PokemonView;
import edu.bu.pas.pokemon.core.Team.TeamView;
import edu.bu.pas.pokemon.core.enums.Type;
import edu.bu.pas.pokemon.core.enums.Stat;
import edu.bu.pas.pokemon.core.enums.NonVolatileStatus;
import edu.bu.pas.pokemon.core.enums.Flag;
import edu.bu.pas.pokemon.linalg.Matrix;


public class CustomSensorArray
        extends SensorArray {

    public CustomSensorArray() {
    }

    public Matrix getSensorValues(final BattleView state, final MoveView action) {

        TeamView myTeam = state.getTeam1View();
        TeamView oppTeam = state.getTeam2View();
        PokemonView myPokemon = myTeam.getActivePokemonView();
        PokemonView oppPokemon = oppTeam.getActivePokemonView();

        double[] features = new double[64];
        int idx = 0;

        features[idx++] = (double) myPokemon.getCurrentStat(Stat.HP) / (double) myPokemon.getInitialStat(Stat.HP);

        features[idx++] = (double) myPokemon.getCurrentStat(Stat.ATK) / 400.0;
        features[idx++] = (double) myPokemon.getCurrentStat(Stat.DEF) / 400.0;
        features[idx++] = (double) myPokemon.getCurrentStat(Stat.SPD) / 400.0;
        features[idx++] = (double) myPokemon.getCurrentStat(Stat.SPATK) / 400.0;
        features[idx++] = (double) myPokemon.getCurrentStat(Stat.SPDEF) / 400.0;

        features[idx++] = (double) myPokemon.getStatMultiplier(Stat.ATK) / 6.0;
        features[idx++] = (double) myPokemon.getStatMultiplier(Stat.DEF) / 6.0;
        features[idx++] = (double) myPokemon.getStatMultiplier(Stat.SPD) / 6.0;
        features[idx++] = (double) myPokemon.getStatMultiplier(Stat.SPATK) / 6.0;
        features[idx++] = (double) myPokemon.getStatMultiplier(Stat.SPDEF) / 6.0;

        features[idx++] = myPokemon.getNonVolatileStatus() == NonVolatileStatus.PARALYSIS ? 1.0 : 0.0;
        features[idx++] = myPokemon.getNonVolatileStatus() == NonVolatileStatus.POISON ||
                myPokemon.getNonVolatileStatus() == NonVolatileStatus.TOXIC ? 1.0 : 0.0;
        features[idx++] = myPokemon.getNonVolatileStatus() == NonVolatileStatus.BURN ? 1.0 : 0.0;
        features[idx++] = myPokemon.getNonVolatileStatus() == NonVolatileStatus.FREEZE ||
                myPokemon.getNonVolatileStatus() == NonVolatileStatus.SLEEP ? 1.0 : 0.0;
        features[idx++] = myPokemon.getFlag(Flag.CONFUSED) ? 1.0 : 0.0;

        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.HP) / (double) oppPokemon.getInitialStat(Stat.HP);

        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.ATK) / 400.0;
        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.DEF) / 400.0;
        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.SPD) / 400.0;
        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.SPATK) / 400.0;
        features[idx++] = (double) oppPokemon.getCurrentStat(Stat.SPDEF) / 400.0;

        features[idx++] = (double) oppPokemon.getStatMultiplier(Stat.ATK) / 6.0;
        features[idx++] = (double) oppPokemon.getStatMultiplier(Stat.DEF) / 6.0;
        features[idx++] = (double) oppPokemon.getStatMultiplier(Stat.SPD) / 6.0;
        features[idx++] = (double) oppPokemon.getStatMultiplier(Stat.SPATK) / 6.0;
        features[idx++] = (double) oppPokemon.getStatMultiplier(Stat.SPDEF) / 6.0;

        features[idx++] = oppPokemon.getNonVolatileStatus() == NonVolatileStatus.PARALYSIS ? 1.0 : 0.0;
        features[idx++] = oppPokemon.getNonVolatileStatus() == NonVolatileStatus.POISON ||
                oppPokemon.getNonVolatileStatus() == NonVolatileStatus.TOXIC ? 1.0 : 0.0;
        features[idx++] = oppPokemon.getNonVolatileStatus() == NonVolatileStatus.BURN ? 1.0 : 0.0;
        features[idx++] = oppPokemon.getNonVolatileStatus() == NonVolatileStatus.FREEZE ||
                oppPokemon.getNonVolatileStatus() == NonVolatileStatus.SLEEP ? 1.0 : 0.0;
        features[idx++] = oppPokemon.getFlag(Flag.CONFUSED) ? 1.0 : 0.0;

        if (action != null) {
            features[idx++] = action.getPower() != null ? (double) action.getPower() / 250.0 : 0.0;

            features[idx++] = action.getAccuracy() != null ? (double) action.getAccuracy() / 100.0 : 1.0;

            features[idx++] = action.getCategory() == Category.PHYSICAL ? 1.0 : 0.0;
            features[idx++] = action.getCategory() == Category.SPECIAL ? 1.0 : 0.0;
            features[idx++] = action.getCategory() == Category.STATUS ? 1.0 : 0.0;

            double typeEff1 = Type.getEffectivenessModifier(action.getType(), oppPokemon.getCurrentType1());
            double typeEff2 = oppPokemon.getCurrentType2() != null ?
                    Type.getEffectivenessModifier(action.getType(), oppPokemon.getCurrentType2()) : 1.0;
            double totalTypeEff = typeEff1 * typeEff2;

            features[idx++] = totalTypeEff >= 2.0 ? 1.0 : 0.0;
            features[idx++] = totalTypeEff == 0.0 ? 1.0 : 0.0;
            features[idx++] = totalTypeEff <= 0.5 && totalTypeEff > 0.0 ? 1.0 : 0.0;

            boolean hasSTAB = action.getType() == myPokemon.getCurrentType1() ||
                    action.getType() == myPokemon.getCurrentType2();
            features[idx++] = hasSTAB ? 1.0 : 0.0;

            features[idx++] = action.getPP() > 0 ? 1.0 : 0.0;

            features[idx++] = (double) action.getPriority() / 5.0;
        } else {
            for (int i = 0; i < 12; i++) {
                features[idx++] = 0.0;
            }
        }

        int myAlive = 0;
        double myTotalHPRatio = 0.0;
        for (int i = 0; i < myTeam.size(); i++) {
            PokemonView p = myTeam.getPokemonView(i);
            if (!p.hasFainted()) {
                myAlive++;
                myTotalHPRatio += (double) p.getCurrentStat(Stat.HP) / (double) p.getInitialStat(Stat.HP);
            }
        }
        features[idx++] = (double) myAlive / 6.0;
        features[idx++] = myTotalHPRatio / 6.0;

        int oppAlive = 0;
        double oppTotalHPRatio = 0.0;
        for (int i = 0; i < oppTeam.size(); i++) {
            PokemonView p = oppTeam.getPokemonView(i);
            if (!p.hasFainted()) {
                oppAlive++;
                oppTotalHPRatio += (double) p.getCurrentStat(Stat.HP) / (double) p.getInitialStat(Stat.HP);
            }
        }
        features[idx++] = (double) oppAlive / 6.0;
        features[idx++] = oppTotalHPRatio / 6.0;

        features[idx++] = ((double) myAlive - (double) oppAlive) / 6.0;
        features[idx++] = (myTotalHPRatio - oppTotalHPRatio) / 6.0;

        features[idx++] = myTeam.getNumReflectTurnsRemaining() > 0 ? 1.0 : 0.0;
        features[idx++] = myTeam.getNumLightScreenTurnsRemaining() > 0 ? 1.0 : 0.0;

        Type myType1 = myPokemon.getCurrentType1();
        Type myType2 = myPokemon.getCurrentType2();
        Type oppType1 = oppPokemon.getCurrentType1();
        Type oppType2 = oppPokemon.getCurrentType2();

        double defMatchup1 = Type.getEffectivenessModifier(oppType1, myType1);
        if (myType2 != null) defMatchup1 *= Type.getEffectivenessModifier(oppType1, myType2);
        if (oppType2 != null) {
            double defMatchup2 = Type.getEffectivenessModifier(oppType2, myType1);
            if (myType2 != null) defMatchup2 *= Type.getEffectivenessModifier(oppType2, myType2);
            defMatchup1 = Math.max(defMatchup1, defMatchup2);
        }
        features[idx++] = defMatchup1 / 4.0;

        double offMatchup = Type.getEffectivenessModifier(myType1, oppType1);
        if (oppType2 != null) offMatchup *= Type.getEffectivenessModifier(myType1, oppType2);
        if (myType2 != null) {
            double offMatchup2 = Type.getEffectivenessModifier(myType2, oppType1);
            if (oppType2 != null) offMatchup2 *= Type.getEffectivenessModifier(myType2, oppType2);
            offMatchup = Math.max(offMatchup, offMatchup2);
        }
        features[idx++] = offMatchup / 4.0;

        int mySpeed = myPokemon.getCurrentStat(Stat.SPD);
        int oppSpeed = oppPokemon.getCurrentStat(Stat.SPD);
        features[idx++] = mySpeed > oppSpeed ? 1.0 : 0.0;
        features[idx++] = (double) (mySpeed - oppSpeed) / 400.0;

        features[idx++] = (double) (myPokemon.getLevel() - oppPokemon.getLevel()) / 100.0;

        features[idx++] = myPokemon.getSubstitute() != null ? 1.0 : 0.0;
        features[idx++] = oppPokemon.getSubstitute() != null ? 1.0 : 0.0;

        features[idx++] = myPokemon.getFlag(Flag.SEEDED) ? 1.0 : 0.0;
        while (idx < 64) {
            features[idx++] = 0.0;
        }
        Matrix sensorVector = Matrix.zeros(1, 64);
        for (int i = 0; i < 64; i++) {
            sensorVector.set(0, i, features[i]);
        }

        return sensorVector;
    }

}