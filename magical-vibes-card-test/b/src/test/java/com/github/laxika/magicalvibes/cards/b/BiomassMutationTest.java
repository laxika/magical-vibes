package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiomassMutation.class, DiscipleOfTheOldWays.class})
class BiomassMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving with X=4 sets base power/toughness of your creatures to 4/4")
    void setsOwnCreaturesToXX() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base P/T is set: a +1/+1 counter still applies on top of the new base")
    void modifiersApplyOnTopOfNewBase() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only affects creatures you control, not the opponent's")
    void doesNotAffectOpponentCreatures() {
        Permanent oppBears = addCreatureReady(player2, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, oppBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oppBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("X=0 sends unmodified creatures to the graveyard but counters can keep creatures alive")
    void zeroXRespectsCountersAndStateBasedActions() {
        addCreatureReady(player1, new DiscipleOfTheOldWays());
        Permanent survivor = addCreatureReady(player1, new DiscipleOfTheOldWays());
        survivor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player1, "Disciple of the Old Ways");
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent original = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, 4, null);
        harness.passBothPriorities();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack are affected at resolution")
    void includesCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, 4, null);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The most recently resolved Mutation sets the base power and toughness")
    void laterResolutionOverridesEarlierBase() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        harness.setHand(player1, List.of(new BiomassMutation(), new BiomassMutation()));
        harness.addMana(player1, ManaColor.GREEN, 11);
        harness.castInstant(player1, 0, 5, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
