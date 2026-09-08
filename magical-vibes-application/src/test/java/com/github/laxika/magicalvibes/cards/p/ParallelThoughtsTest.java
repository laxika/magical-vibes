package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParallelThoughts.class, Forest.class, GrizzlyBears.class})
class ParallelThoughtsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven cards exiled face down in a source-tracked pile")
    void etbExilesSevenCardsIntoPile() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new ParallelThoughts()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player1, 0);
        }

        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).hasSize(7);
        assertThat(gd.exiledCards).filteredOn(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .allMatch(e -> e.faceDown());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May replace a draw with the top card of its exiled pile")
    void acceptsDrawReplacementFromPile() {
        UUID sourcePermanentId = setupWithPile(List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.getCardsExiledByPermanent(sourcePermanentId)).isEmpty();
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Declining the replacement performs a normal draw")
    void declinesDrawReplacement() {
        setupWithPile(List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Accepting with an empty pile still replaces the draw")
    void emptyPileStillReplacesDraw() {
        setupWithPile(List.of());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveDrawChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.winnerPlayerId).isNull();
    }

    private UUID setupWithPile(List<Card> pile) {
        harness.addToBattlefield(player1, new ParallelThoughts());
        UUID sourcePermanentId = harness.getPermanentId(player1, "Parallel Thoughts");
        for (Card card : pile) {
            gd.addToExile(player1.getId(), card, sourcePermanentId, true);
        }
        return sourcePermanentId;
    }

    private void resolveDrawChoice() {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
