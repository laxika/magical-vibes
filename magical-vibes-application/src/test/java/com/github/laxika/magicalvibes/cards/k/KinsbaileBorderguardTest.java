package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinsbaileBorderguardTest extends BaseCardTest {

    // ===== ETB counter placement =====

    @Test
    @DisplayName("Enters with 0 counters when no other Kithkin are controlled")
    void entersWithNoCountersWithoutOtherKithkin() {
        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent borderguard = findBorderguard(player1);
        assertThat(borderguard).isNotNull();
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other Kithkin controlled")
    void entersWithCountersPerOtherKithkin() {
        harness.addToBattlefield(player1, new GoldmeadowStalwart());
        harness.addToBattlefield(player1, new GoldmeadowStalwart());

        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent borderguard = findBorderguard(player1);
        assertThat(borderguard).isNotNull();
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count the Borderguard itself or opponent's Kithkin")
    void doesNotCountSelfOrOpponentKithkin() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());

        harness.setHand(player1, List.of(new KinsbaileBorderguard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent borderguard = findBorderguard(player1);
        assertThat(borderguard).isNotNull();
        assertThat(borderguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When it dies, creates a 1/1 white Kithkin Soldier token for each counter on it")
    void deathCreatesTokenPerCounter() {
        harness.addToBattlefield(player1, new KinsbaileBorderguard());
        Permanent borderguard = findBorderguard(player1);
        borderguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Borderguard dies

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kinsbaile Borderguard"));
        assertThat(gd.stack).hasSize(1); // death trigger on the stack

        harness.passBothPriorities(); // resolve death trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Kithkin Soldier"))
                .toList();
        assertThat(tokens).hasSize(3);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("When it dies with no counters, creates no tokens")
    void deathWithNoCountersCreatesNoTokens() {
        harness.addToBattlefield(player1, new KinsbaileBorderguard());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Kithkin Soldier"))
                .toList();
        assertThat(tokens).isEmpty();
    }

    // ===== Helpers =====

    private Permanent findBorderguard(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kinsbaile Borderguard"))
                .findFirst().orElse(null);
    }
}
