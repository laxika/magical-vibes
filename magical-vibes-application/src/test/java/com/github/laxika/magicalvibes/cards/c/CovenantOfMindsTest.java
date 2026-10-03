package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CovenantOfMinds.class, DruidOfTheAnima.class, AlmsCollector.class})
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
        // The revealed cards were put into hand, not drawn â€” nothing extra was drawn.
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


    @Test
    void acceptsAllAvailableCardsFromShortLibrary() {
        setupAndCast();
        setupLibrary(2);
        List<Card> available = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(available);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void acceptsEmptyLibraryWithoutDrawing() {
        setupAndCast();
        setupLibrary(0);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void secondPlayerCasterReceivesCardsChosenByFirstPlayer() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CovenantOfMinds()));
        List<Card> available = List.of(new DruidOfTheAnima(), new DruidOfTheAnima());
        harness.setLibrary(player2, available);
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(available);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void declineDrawFiveIsReplacedByAlmsCollector() {
        setupAndCast();
        setupLibrary(10);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new DruidOfTheAnima()));
        harness.addToBattlefield(player2, new AlmsCollector());
        List<UUID> revealedIds = topThreeIds(gd);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).containsAll(revealedIds);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CovenantOfMinds()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void setupLibrary(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new DruidOfTheAnima());
        }
        harness.setLibrary(player1, deck);
    }

    private List<UUID> topThreeIds(GameData gd) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        return List.of(deck.get(0).getId(), deck.get(1).getId(), deck.get(2).getId());
    }
}
