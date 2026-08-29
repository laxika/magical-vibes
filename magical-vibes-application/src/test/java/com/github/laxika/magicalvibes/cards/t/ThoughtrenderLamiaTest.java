package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtrenderLamiaTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent discards a card when Thoughtrender Lamia enters")
    void ownEntryTriggers() {
        harness.setHand(player1, List.of(new ThoughtrenderLamia()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each opponent discards a card when another enchantment enters under your control")
    void anotherEnchantmentEntryTriggers() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment creature enters under your control")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's enchantment enters")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThoughtrenderLamia());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
