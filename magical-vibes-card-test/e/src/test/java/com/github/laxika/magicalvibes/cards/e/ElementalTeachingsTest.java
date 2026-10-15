package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalTeachings.class, Forest.class, Island.class, Mountain.class, Plains.class, RestInPeace.class})
class ElementalTeachingsTest extends BaseCardTest {

    private void castElementalTeachings(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ElementalTeachings()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
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
    void opponentChoosesTwoForGraveyardAndRestEnterTapped() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card nonland = new ElementalTeachings();
        castElementalTeachings(List.of(island, forest, mountain, plains, nonland));

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == island && permanent.isTapped())
                .anyMatch(permanent -> permanent.getCard() == mountain && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest || permanent.getCard() == plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        harness.assertInGraveyard(player1, "Elemental Teachings");
    }

    @Test
    void libraryWithNoLandsFinishesWithoutAnOpponentChoice() {
        Card nonland = new ElementalTeachings();
        castElementalTeachings(List.of(nonland));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Elemental Teachings");
    }

    @Test
    void mayFindNoLandsEvenWhenLandsAreAvailable() {
        Card island = new Island();
        castElementalTeachings(List.of(island));
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Elemental Teachings");
    }

    @Test
    void cannotFindTwoLandsWithTheSameName() {
        Card firstIsland = new Island();
        Card secondIsland = new Island();
        Card forest = new Forest();
        castElementalTeachings(List.of(firstIsland, secondIsland, forest));
        pickFromLibrary("Island");
        assertThat(offeredNames()).containsExactly("Forest");
        pickFromLibrary("Forest");
        harness.handleMultipleCardsChosen(player2, List.of(firstIsland.getId(), forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstIsland, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondIsland);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void mayStopAtThreeLandsAndPutTheUnchosenLandOntoBattlefieldTapped() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        castElementalTeachings(List.of(island, forest, mountain, plains));
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        harness.handleCardChosen(player1, -1);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(mountain);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void restInPeaceExilesTheLandsChosenForTheGraveyard() {
        harness.addToBattlefield(player2, new RestInPeace());
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        castElementalTeachings(List.of(island, forest, mountain));
        pickFromLibrary("Island");
        pickFromLibrary("Forest");
        pickFromLibrary("Mountain");
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(island, forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(mountain);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    void findingOneLandPutsItIntoTheGraveyard() {
        Card island = new Island();
        castElementalTeachings(List.of(island, new ElementalTeachings()));

        pickFromLibrary("Island");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
