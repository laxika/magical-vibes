package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FarWanderings.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class FarWanderingsTest extends BaseCardTest {

    @Test
    @DisplayName("Below threshold, Far Wanderings searches for one basic land")
    void belowThresholdSearchesForOneBasicLand() {
        setupLibrary();
        castFarWanderings();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Below threshold, Far Wanderings puts one basic land onto the battlefield tapped")
    void belowThresholdPutsOneBasicLandOntoBattlefieldTapped() {
        setupLibrary();
        castFarWanderings();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
    }

    @Test
    @DisplayName("With six cards in the graveyard, Far Wanderings still searches for one basic land")
    void sixGraveyardCardsDoNotMeetThreshold() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        setupLibrary();
        castFarWanderings();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("At threshold, Far Wanderings searches for up to three basic lands")
    void atThresholdSearchesForUpToThreeBasicLands() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        setupLibrary();
        castFarWanderings();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(3)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("At threshold, Far Wanderings can put fewer than three basic lands onto the battlefield")
    void atThresholdSearchesForOnlyAvailableBasicLands() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));
        castFarWanderings();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(3);
        assertThat(search.params().cards()).hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Far Wanderings resolves without finding a land when the library has no basic lands")
    void resolvesWithoutFindingABasicLand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castFarWanderings();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    private void castFarWanderings() {
        harness.setHand(player1, List.of(new FarWanderings()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.Stream.generate(() -> (Card) new GrizzlyBears()).limit(count).toList();
    }
}
