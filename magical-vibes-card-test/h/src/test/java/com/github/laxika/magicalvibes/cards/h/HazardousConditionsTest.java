package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrazenScourge;
import com.github.laxika.magicalvibes.cards.d.DhundOperative;
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

@CardUsed({HazardousConditions.class, BrazenScourge.class, DhundOperative.class})
class HazardousConditionsTest extends BaseCardTest {

    private void castHazardousConditions() {
        harness.setHand(player1, List.of(new HazardousConditions()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    @Test
    @DisplayName("Gives creatures with no counters on them -2/-2 on both sides")
    void weakensOnlyCreaturesWithoutCounters() {
        Permanent ownUncountered = harness.addToBattlefieldAndReturn(player1, new BrazenScourge());
        Permanent ownCountered = harness.addToBattlefieldAndReturn(player1, new BrazenScourge());
        ownCountered.setCounterCount(CounterType.CHARGE, 1);
        Permanent opponentUncountered = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());

        castHazardousConditions();

        assertThat(gqs.getEffectivePower(gd, ownUncountered)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownUncountered)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentUncountered)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentUncountered)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownCountered)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCountered)).isEqualTo(3);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());

        castHazardousConditions();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void counterChangesAfterResolutionDoNotChangeAffectedCreatures() {
        Permanent affected = harness.addToBattlefieldAndReturn(player1, new BrazenScourge());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());
        protectedCreature.setCounterCount(CounterType.CHARGE, 1);

        castHazardousConditions();
        affected.setCounterCount(CounterType.CHARGE, 1);
        protectedCreature.setCounterCount(CounterType.CHARGE, 0);

        assertThat(gqs.getEffectivePower(gd, affected)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, affected)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, protectedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, protectedCreature)).isEqualTo(3);
    }

    @Test
    void checksCountersAtResolutionRatherThanCasting() {
        Permanent gainsCounter = harness.addToBattlefieldAndReturn(player1, new BrazenScourge());
        Permanent losesCounter = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());
        losesCounter.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new HazardousConditions()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());

        gainsCounter.setCounterCount(CounterType.CHARGE, 1);
        losesCounter.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gainsCounter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gainsCounter)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, losesCounter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, losesCounter)).isEqualTo(1);
    }

    @Test
    void creaturesEnteringAfterResolutionAreUnaffected() {
        castHazardousConditions();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new BrazenScourge());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);
    }

    @Test
    void zeroToughnessKillsUncounteredCreatureButPlusOneCounterProtectsCreature() {
        harness.addToBattlefield(player1, new DhundOperative());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new DhundOperative());
        protectedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHazardousConditions();

        harness.assertNotOnBattlefield(player1, "Dhund Operative");
        harness.assertInGraveyard(player1, "Dhund Operative");
        harness.assertOnBattlefield(player2, "Dhund Operative");
        assertThat(gqs.getEffectivePower(gd, protectedCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, protectedCreature)).isEqualTo(3);
    }
}
