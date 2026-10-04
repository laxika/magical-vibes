package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvolvedSpinoderm.class})
class EvolvedSpinodermTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four oil counters and has hexproof")
    void entersWithOilCountersAndHexproof() {
        harness.setHand(player1, List.of(new EvolvedSpinoderm()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spinoderm = findPermanent(player1, "Evolved Spinoderm");
        assertThat(spinoderm.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample with two or fewer oil counters and otherwise has hexproof")
    void switchesKeywordsAtThreeOilCounters() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());

        spinoderm.setCounterCount(CounterType.OIL, 2);
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.HEXPROOF)).isFalse();

        spinoderm.setCounterCount(CounterType.OIL, 3);
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Removes one oil counter during its controller's upkeep")
    void removesOilCounterAtUpkeep() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());
        spinoderm.setCounterCount(CounterType.OIL, 4);

        advanceToPlayerOneUpkeep();

        assertThat(spinoderm.getCounterCount(CounterType.OIL)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Evolved Spinoderm");
    }

    @Test
    @DisplayName("Sacrifices itself when the upkeep removes its last oil counter")
    void sacrificesWhenLastOilCounterIsRemoved() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());
        spinoderm.setCounterCount(CounterType.OIL, 1);

        advanceToPlayerOneUpkeep();

        assertThat(spinoderm.getCounterCount(CounterType.OIL)).isZero();
        harness.assertNotOnBattlefield(player1, "Evolved Spinoderm");
        harness.assertInGraveyard(player1, "Evolved Spinoderm");
    }

    @Test
    @DisplayName("Zero oil counters grant trample without sacrificing before upkeep")
    void zeroCountersGrantTrampleUntilUpkeep() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());
        spinoderm.setCounterCount(CounterType.OIL, 0);

        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.HEXPROOF)).isFalse();
        harness.assertOnBattlefield(player1, "Evolved Spinoderm");

        advanceToPlayerOneUpkeep();

        harness.assertNotOnBattlefield(player1, "Evolved Spinoderm");
        harness.assertInGraveyard(player1, "Evolved Spinoderm");
    }

    @Test
    @DisplayName("Does not remove oil counters during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveCounters() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());
        spinoderm.setCounterCount(CounterType.OIL, 1);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(spinoderm.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Evolved Spinoderm");
    }

    @Test
    @DisplayName("Upkeep changes hexproof to trample when three oil counters become two")
    void upkeepSwitchesKeywordsAtThreshold() {
        Permanent spinoderm = addCreatureReady(player1, new EvolvedSpinoderm());
        spinoderm.setCounterCount(CounterType.OIL, 3);

        advanceToPlayerOneUpkeep();

        assertThat(spinoderm.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spinoderm, Keyword.HEXPROOF)).isFalse();
        harness.assertOnBattlefield(player1, "Evolved Spinoderm");
    }

    private void advanceToPlayerOneUpkeep() {
        advanceToUpkeep(player1);
        resolveAllTriggers();
    }
}
