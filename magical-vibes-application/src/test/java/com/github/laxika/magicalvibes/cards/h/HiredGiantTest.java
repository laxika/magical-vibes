package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HiredGiantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers each opponent a land search, not the controller")
    void etbOffersOpponentLandSearch() {
        setupOpponentLibrary(player2, new EvolvingWilds(), new GrizzlyBears(), new Forest());
        castHiredGiant();
        resolveEtb();

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Opponent may put a nonbasic land onto the battlefield untapped")
    void opponentMayPutNonbasicLandUntapped() {
        setupOpponentLibrary(player2, new EvolvingWilds(), new GrizzlyBears());
        castHiredGiant();
        resolveEtb();

        PendingInteraction.LibrarySearch search = activeSearch();
        int landIndex = indexOf(search, EvolvingWilds.class);
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player2, new InteractionAnswer.LibraryCardChosen(landIndex));

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof EvolvingWilds && !permanent.isTapped());
        assertThat(activeSearch()).isNull();
    }

    @Test
    @DisplayName("Opponent may decline the land search")
    void opponentMayDecline() {
        setupOpponentLibrary(player2, new Forest());
        castHiredGiant();
        resolveEtb();

        GameData gameData = harness.getGameData();
        int before = gameData.playerBattlefields.get(player2.getId()).size();
        harness.getGameService().handleInteractionAnswer(
                gameData, player2, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gameData.playerBattlefields.get(player2.getId())).hasSize(before);
        assertThat(activeSearch()).isNull();
    }

    @Test
    @DisplayName("No land search is offered when an opponent has no land cards")
    void noLandNoPrompt() {
        setupOpponentLibrary(player2, new GrizzlyBears());
        castHiredGiant();
        resolveEtb();

        assertThat(activeSearch()).isNull();
    }

    private void castHiredGiant() {
        harness.setHand(player1, List.of(new HiredGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private int indexOf(PendingInteraction.LibrarySearch search, Class<? extends Card> cardType) {
        for (int i = 0; i < search.params().cards().size(); i++) {
            if (cardType.isInstance(search.params().cards().get(i))) {
                return i;
            }
        }
        throw new IllegalStateException("No matching card in search options");
    }

    private void setupOpponentLibrary(Player player, Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
