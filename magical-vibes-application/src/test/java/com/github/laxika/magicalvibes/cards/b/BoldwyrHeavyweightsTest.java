package com.github.laxika.magicalvibes.cards.b;

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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoldwyrHeavyweights.class, Forest.class, GrizzlyBears.class, Plains.class})
class BoldwyrHeavyweightsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts the opponent to search for a creature card to battlefield")
    void etbPromptsOpponentCreatureSearch() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();
        harness.handleMayAbilityChosen(player2, true);

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
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();
        int before = gd.playerBattlefields.get(player2.getId()).size();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        int bearsIndex = indexOfCreature(search);

        harness.handleCardChosen(player2, bearsIndex);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before + 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.CREATURE) && !p.isTapped());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Opponent may decline without searching their library")
    void opponentCanDecline() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();

        GameData gd = harness.getGameData();
        int before = gd.playerBattlefields.get(player2.getId()).size();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(before);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player2.getId());
    }

    @Test
    @DisplayName("A library with no creatures still offers an optional search")
    void noCreaturesStillOffersOptionalSearch() {
        castHeavyweights();
        harness.setLibrary(player2, List.of(new Plains(), new Forest()));
        resolveEtb();

        GameData gd = harness.getGameData();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Offering a search does not count as searching before the opponent chooses")
    void offeringSearchDoesNotCountAsSearching() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player2.getId());
    }

    @Test
    @DisplayName("An empty library does not automatically count as an accepted search")
    void emptyLibraryDoesNotAutomaticallySearch() {
        castHeavyweights();
        harness.setLibrary(player2, List.of());
        resolveEtb();

        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("A selected creature leaves the library and only the opponent searches")
    void selectedCreatureLeavesLibrary() {
        castHeavyweights();
        setupOpponentLibrary(player2);
        resolveEtb();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, indexOfCreature(search));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Forest");
        assertThat(gd.playersWhoSearchedLibraryThisTurn)
                .contains(player2.getId()).doesNotContain(player1.getId());
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
        harness.setLibrary(player, List.of(new Plains(), new GrizzlyBears(), new Forest()));
    }
}
