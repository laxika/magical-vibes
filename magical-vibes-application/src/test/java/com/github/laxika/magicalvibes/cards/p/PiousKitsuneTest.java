package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EightAndAHalfTails;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PiousKitsuneTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a devotion counter on itself at the beginning of its controller's upkeep")
    void putsDevotionCounterOnUpkeep() {
        Permanent kitsune = addReadyPermanent(player1, new PiousKitsune());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains life equal to its devotion counters when a player controls Eight-and-a-Half-Tails")
    void gainsLifeWithEightAndAHalfTails() {
        Permanent kitsune = addReadyPermanent(player1, new PiousKitsune());
        addReadyPermanent(player2, new EightAndAHalfTails());
        kitsune.setCounterCount(CounterType.DEVOTION, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Tapping and removing a devotion counter gains 1 life")
    void removesCounterAndGainsLife() {
        Permanent kitsune = addReadyPermanent(player1, new PiousKitsune());
        kitsune.setCounterCount(CounterType.DEVOTION, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kitsune.isTapped()).isTrue();
        assertThat(kitsune.getCounterCount(CounterType.DEVOTION)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate without a devotion counter")
    void cannotActivateWithoutCounter() {
        addReadyPermanent(player1, new PiousKitsune());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
