package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OliviasWrath.class, GrizzlyBears.class, HillGiant.class, VampireNoble.class})
class OliviasWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Gives non-Vampire creatures -X/-X based on Vampires its controller controls")
    void givesNonVampireCreaturesMinusNumberOfVampiresYouControl() {
        Permanent ownVampire = harness.addToBattlefieldAndReturn(player1, new VampireNoble());
        Permanent secondOwnVampire = harness.addToBattlefieldAndReturn(player1, new VampireNoble());
        Permanent opposingVampire = harness.addToBattlefieldAndReturn(player2, new VampireNoble());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        int ownVampirePower = ownVampire.getEffectivePower();
        int ownVampireToughness = ownVampire.getEffectiveToughness();
        int secondOwnVampirePower = secondOwnVampire.getEffectivePower();
        int opposingVampirePower = opposingVampire.getEffectivePower();

        castOliviasWrath();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownVampire.getEffectivePower()).isEqualTo(ownVampirePower);
        assertThat(ownVampire.getEffectiveToughness()).isEqualTo(ownVampireToughness);
        assertThat(secondOwnVampire.getEffectivePower()).isEqualTo(secondOwnVampirePower);
        assertThat(opposingVampire.getEffectivePower()).isEqualTo(opposingVampirePower);
    }

    @Test
    @DisplayName("Kills non-Vampire creatures whose toughness is reduced to zero")
    void killsNonVampireCreaturesWithZeroToughness() {
        harness.addToBattlefield(player1, new VampireNoble());
        harness.addToBattlefield(player1, new VampireNoble());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castOliviasWrath();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new VampireNoble());

        castOliviasWrath();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opposing Vampires do not contribute to X")
    void noControlledVampiresMeansNoReduction() {
        harness.addToBattlefield(player2, new VampireNoble());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOliviasWrath();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X is determined on resolution and remains fixed afterward")
    void countsVampiresAtResolutionAndLocksInReduction() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareOliviasWrath();
        harness.castSorcery(player1, 0);
        harness.addToBattlefield(player1, new VampireNoble());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);

        harness.addToBattlefield(player1, new VampireNoble());

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new VampireNoble());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castOliviasWrath();
        Permanent newCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThat(existingCreature.getEffectivePower()).isEqualTo(2);
        assertThat(existingCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(newCreature.getEffectivePower()).isEqualTo(3);
        assertThat(newCreature.getEffectiveToughness()).isEqualTo(3);
    }

    private void castOliviasWrath() {
        prepareOliviasWrath();
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    private void prepareOliviasWrath() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OliviasWrath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
