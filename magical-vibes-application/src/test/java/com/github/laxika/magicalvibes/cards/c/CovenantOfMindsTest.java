package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CovenantOfMindsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving prompts the targeted opponent to choose")
    void resolvingPromptsOpponentChoice() {
        setupAndCast();

        harness.passBothPriorities(); // resolve sorcery

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accept puts the three revealed cards into the controller's hand")
    void acceptPutsRevealedCardsIntoHand() {
        setupAndCast();
        setupLibrary(8);

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        List<UUID> revealedIds = topThreeIds(gd);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsAll(revealedIds);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
        // The revealed cards were put into hand, not drawn — nothing extra was drawn.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Decline mills the revealed cards and the controller draws five")
    void declineMillsAndDrawsFive() {
        setupAndCast();
        setupLibrary(10);

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        List<UUID> revealedIds = topThreeIds(gd);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player2, false);

        // The three revealed cards go to the controller's graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).containsAll(revealedIds);
        // The controller draws five cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContainAnyElementsOf(revealedIds);
        // Three revealed + five drawn removed from the library.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 8);
    }

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CovenantOfMinds()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void setupLibrary(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(deck);
    }

    private List<UUID> topThreeIds(GameData gd) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        return List.of(deck.get(0).getId(), deck.get(1).getId(), deck.get(2).getId());
    }
}
