package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HotheadedGiantTest extends BaseCardTest {

    @Test
    @DisplayName("No other red spell cast this turn — enters with two -1/-1 counters")
    void entersWithCountersWhenNoPriorRedSpell() {
        harness.setHand(player1, List.of(new HotheadedGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findGiant(player1);
        assertThat(giant).isNotNull();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Another red spell cast this turn — enters with no counters")
    void entersWithoutCountersAfterPriorRedSpell() {
        harness.setHand(player1, List.of(new RagingGoblin(), new HotheadedGiant()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0); // Raging Goblin — a red spell
        harness.passBothPriorities();

        harness.castCreature(player1, 0); // Hotheaded Giant
        harness.passBothPriorities();

        Permanent giant = findGiant(player1);
        assertThat(giant).isNotNull();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A prior non-red spell does not prevent the counters")
    void entersWithCountersAfterNonRedSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new HotheadedGiant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0); // Grizzly Bears — a green spell
        harness.passBothPriorities();

        harness.castCreature(player1, 0); // Hotheaded Giant
        harness.passBothPriorities();

        Permanent giant = findGiant(player1);
        assertThat(giant).isNotNull();
        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private Permanent findGiant(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hotheaded Giant"))
                .findFirst().orElse(null);
    }
}
