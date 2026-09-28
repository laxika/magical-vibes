package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EightAndAHalfTails;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiousKitsune.class, EightAndAHalfTails.class})
class PiousKitsuneTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a devotion counter on itself at the beginning of its controller's upkeep")
    void putsDevotionCounterOnUpkeep() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not put a devotion counter on the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isZero();
    }

    @Test
    @DisplayName("Gains life equal to its devotion counters when a player controls Eight-and-a-Half-Tails")
    void gainsLifeWithEightAndAHalfTails() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());
        addCreatureReady(player2, new EightAndAHalfTails());
        kitsune.setCounterCount(CounterType.DEVOTION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Tapping and removing a devotion counter gains 1 life")
    void removesCounterAndGainsLife() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());
        kitsune.setCounterCount(CounterType.DEVOTION, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kitsune.isTapped()).isTrue();
        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate the tap ability while Pious Kitsune is tapped")
    void cannotActivateWhileTapped() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());
        kitsune.setCounterCount(CounterType.DEVOTION, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Cannot activate without a devotion counter")
    void cannotActivateWithoutCounter() {
        Permanent kitsune = addCreatureReady(player1, new PiousKitsune());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kitsune.isTapped()).isFalse();
    }
}
