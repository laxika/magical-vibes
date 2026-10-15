package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.o.Oust;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealmsUncharted.class, Forest.class, Island.class, Mountain.class, Plains.class,
        Oust.class, LeylineOfTheVoid.class})
class RealmsUnchartedTest extends BaseCardTest {

    private void castRealmsUncharted(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new RealmsUncharted()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
    }

    private List<String> offeredNames() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }

    private void pickFromLibrary(String name) {
        int index = offeredNames().indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }

    @Test
    @DisplayName("Offers only distinct-name lands and an opponent chooses two for the graveyard")
    void searchesLandsAndOpponentChoosesTwo() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card nonland = new Oust();
        castRealmsUncharted(List.of(island, forest, mountain, plains, nonland));

        assertThat(offeredNames()).containsExactlyInAnyOrder("Island", "Forest", "Mountain", "Plains");
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        pickFromLibrary("Plains");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(forest.getId(), plains.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerHands.get(player1.getId())).contains(island, mountain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        harness.assertInGraveyard(player1, "Realms Uncharted");
    }

    @Test
    @DisplayName("Nonland cards are excluded from later distinct-name offers")
    void onlyLandsAreOfferedThroughoutTheSearch() {
        castRealmsUncharted(List.of(new Island(), new Oust(), new Forest()));

        assertThat(offeredNames()).containsExactlyInAnyOrder("Island", "Forest");
        pickFromLibrary("Island");

        assertThat(offeredNames()).containsExactly("Forest");
    }

    @Test
    @DisplayName("Revealing fewer than four lands puts one or two revealed lands into the graveyard")
    void fewerThanFourLandsStillUsesTheRevealedPool() {
        Card island = new Island();
        Card forest = new Forest();
        castRealmsUncharted(List.of(island, forest));

        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFindZeroEvenWhenLandsAreAvailable() {
        Card island = new Island();
        castRealmsUncharted(List.of(island));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Realms Uncharted");
    }

    @Test
    void canStopAfterOneLandWhileOtherLandsRemain() {
        Card island = new Island();
        Card forest = new Forest();
        castRealmsUncharted(List.of(island, forest));
        pickFromLibrary("Island");
        harness.handleCardChosen(player1, -1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void excludesOtherCopiesOfAlreadyRevealedNames() {
        Card island = new Island();
        Card otherIsland = new Island();
        Card forest = new Forest();
        castRealmsUncharted(List.of(island, otherIsland, forest));
        pickFromLibrary("Island");

        assertThat(offeredNames()).containsExactly("Forest");
        pickFromLibrary("Forest");
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherIsland);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void threeRevealedLandsLeaveOneForTheHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        castRealmsUncharted(List.of(island, forest, mountain));
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithoutMatchingLands() {
        Card nonland = new Oust();
        castRealmsUncharted(List.of(nonland));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Realms Uncharted");
    }

    @Test
    void chosenLandsRespectGraveyardReplacementEffects() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        castRealmsUncharted(List.of(island, forest, mountain));
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mountain);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
