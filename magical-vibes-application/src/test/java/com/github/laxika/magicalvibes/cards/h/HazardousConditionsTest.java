package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HazardousConditionsTest extends BaseCardTest {

    private void castHazardousConditions() {
        harness.setHand(player1, List.of(new HazardousConditions()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gives creatures with no counters on them -2/-2 on both sides")
    void weakensOnlyCreaturesWithoutCounters() {
        Permanent ownUncountered = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownCountered = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        ownCountered.setCounterCount(CounterType.CHARGE, 1);
        Permanent opponentUncountered = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castHazardousConditions();

        assertThat(ownUncountered.getEffectivePower()).isEqualTo(1);
        assertThat(ownUncountered.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentUncountered.getEffectivePower()).isEqualTo(1);
        assertThat(opponentUncountered.getEffectiveToughness()).isEqualTo(1);
        assertThat(ownCountered.getEffectivePower()).isEqualTo(3);
        assertThat(ownCountered.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castHazardousConditions();

        assertThat(giant.getEffectivePower()).isEqualTo(1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }
}
