package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThassasDevourer.class, Forest.class, GloriousAnthem.class})
class ThassasDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes a target player mill two cards")
    void ownEntryMillsTargetPlayer() {
        setLibrary(player2, 3);
        castThassasDevourer(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers the ability")
    void anotherEnchantmentEntryMillsTargetPlayer() {
        harness.addToBattlefield(player1, new ThassasDevourer());
        setLibrary(player2, 3);
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThassasDevourer());
        setLibrary(player2, 3);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can be targeted and mills the top two cards in order")
    void canMillController() {
        Card first = new ThassasDevourer();
        Card second = new ThassasDevourer();
        Card third = new ThassasDevourer();
        harness.setLibrary(player1, List.of(first, second, third));
        castThassasDevourer(player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with one card mills only that card")
    void millsShortLibrary() {
        setLibrary(player2, 1);
        castThassasDevourer(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with an empty library remains a legal target")
    void canTargetEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        castThassasDevourer(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Devourer triggers both its own and the existing Devourer's abilities")
    void enchantmentCreatureEntryTriggersBothDevourers() {
        harness.addToBattlefield(player1, new ThassasDevourer());
        setLibrary(player1, 3);
        setLibrary(player2, 3);
        castThassasDevourer(player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A land entering does not trigger constellation")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThassasDevourer());
        setLibrary(player2, 3);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castThassasDevourer(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ThassasDevourer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, targetPlayerId);
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, int size) {
        harness.setLibrary(player, List.<Card>of(new Forest(), new Forest(), new Forest()).subList(0, size));
    }
}
