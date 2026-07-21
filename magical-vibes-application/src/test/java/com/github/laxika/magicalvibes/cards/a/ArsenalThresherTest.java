package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArsenalThresherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with no counters when no other artifact cards are in hand")
    void entersWithNoCountersWhenNoArtifacts() {
        harness.setHand(player1, List.of(new ArsenalThresher()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findThresher(player1);
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other artifact card in hand")
    void entersWithCounterPerArtifactCard() {
        harness.setHand(player1, List.of(
                new ArsenalThresher(), new DarksteelRelic(), new Ornithopter()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findThresher(player1);
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-artifact cards in hand are not counted")
    void doesNotCountNonArtifactCards() {
        harness.setHand(player1, List.of(
                new ArsenalThresher(), new DarksteelRelic(), new GrizzlyBears()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findThresher(player1);
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the controller's hand is counted, not the opponent's artifact cards")
    void doesNotCountOpponentArtifactCards() {
        harness.setHand(player1, List.of(new ArsenalThresher()));
        harness.setHand(player2, List.of(new DarksteelRelic(), new Ornithopter()));
        payMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thresher = findThresher(player1);
        assertThat(thresher).isNotNull();
        assertThat(thresher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    // {2}{W/B}{U}: blue covers {U} and the two generic, white pays the {W/B} hybrid.
    private void payMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 3);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent findThresher(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Arsenal Thresher"))
                .findFirst().orElse(null);
    }
}
