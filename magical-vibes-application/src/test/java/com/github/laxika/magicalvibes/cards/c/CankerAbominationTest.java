package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CankerAbominationTest extends BaseCardTest {

    private void castCanker() {
        harness.setHand(player1, List.of(new CankerAbomination()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {2}{B/G}{B/G}
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enters with a -1/-1 counter for each creature the opponent controls")
    void entersWithCountersFromOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castCanker();

        Permanent canker = findCanker(player1);
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enters with no counters when the opponent controls no creatures")
    void entersWithNoCountersWhenOpponentHasNoCreatures() {
        castCanker();

        Permanent canker = findCanker(player1);
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count creatures the controller controls")
    void doesNotCountOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castCanker();

        Permanent canker = findCanker(player1);
        assertThat(canker).isNotNull();
        assertThat(canker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dies to SBA when opponent controls enough creatures to zero its toughness")
    void diesWhenEnoughOpponentCreatures() {
        // 6/6 base; six opponent creatures put six -1/-1 counters on it → 0/0
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        castCanker();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Canker Abomination"));
    }

    private Permanent findCanker(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Canker Abomination"))
                .findFirst().orElse(null);
    }
}
