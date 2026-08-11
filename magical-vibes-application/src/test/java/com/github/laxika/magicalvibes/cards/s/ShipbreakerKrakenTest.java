package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipbreakerKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts four +1/+1 counters on Shipbreaker Kraken")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent kraken = addReadyKraken();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(kraken.isMonstrous()).isTrue();
        assertThat(kraken.getEffectivePower()).isEqualTo(10);
        assertThat(kraken.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("Becoming monstrous taps up to four creatures and locks them while the Kraken remains")
    void becomingMonstrousTapsAndLocksFourCreatures() {
        Permanent kraken = addReadyKraken();
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        }
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();

        assertThat(targets).allMatch(Permanent::isTapped);

        advanceToNextTurn(player1);

        assertThat(targets).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Monstrosity cannot be activated again after the Kraken becomes monstrous")
    void monstrosityOnlyResolvesOnce() {
        addReadyKraken();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already monstrous");
    }

    private Permanent addReadyKraken() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ShipbreakerKraken());
        kraken.setSummoningSick(false);
        return kraken;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
