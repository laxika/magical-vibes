package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoldwyrHeavyweightsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts the opponent to search for a creature card to battlefield")
    void etbPromptsOpponentCreatureSearch() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        // The opponent, not the controller, searches.
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        // Only creature cards are offered, and they go onto the battlefield.
        assertThat(search.params().cards()).allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Opponent picks a creature and it enters their battlefield untapped")
    void opponentCreatureEntersUntapped() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();

        GameData gd = harness.getGameData();
        int before = gd.playerBattlefields.get(player2.getId()).size();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int bearsIndex = indexOfCreature(search);

        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(bearsIndex));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before + 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.CREATURE) && !p.isTapped());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Opponent may decline the search (fail to find)")
    void opponentCanDecline() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();

        GameData gd = harness.getGameData();
        int before = gd.playerBattlefields.get(player2.getId()).size();

        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No prompt when opponent has no creature cards in library")
    void noCreaturesNoPrompt() {
        castHeavyweights();
        List<Card> deck = harness.getGameData().playerDecks.get(player2.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest()));
        resolveEtb();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void castHeavyweights() {
        harness.setHand(player1, List.of(new BoldwyrHeavyweights()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities(); // resolve creature spell, ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int indexOfCreature(PendingInteraction.LibrarySearch search) {
        List<Card> cards = search.params().cards();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).hasType(CardType.CREATURE)) {
                return i;
            }
        }
        throw new IllegalStateException("No creature in search options");
    }

    private void setupOpponentLibrary(Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new GrizzlyBears(), new Forest()));
    }
}
