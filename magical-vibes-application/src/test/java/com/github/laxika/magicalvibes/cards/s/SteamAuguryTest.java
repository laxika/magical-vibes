package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamAugury.class, Island.class, Forest.class, Mountain.class, Plains.class, Swamp.class})
class SteamAuguryTest extends BaseCardTest {

    private void castSteamAugury(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new SteamAugury(), "{2}{U}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller separates the top five cards and the opponent chooses a pile")
    void controllerSeparatesOpponentChooses() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card swamp = new Swamp();
        castSteamAugury(List.of(island, forest, mountain, plains, swamp));

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation).isNotNull();
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validCardIds()).containsExactlyInAnyOrder(
                island.getId(), forest.getId(), mountain.getId(), plains.getId(), swamp.getId());

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mountain, plains, swamp);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("The opponent can choose the other pile")
    void opponentCanChoosePileTwo() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card swamp = new Swamp();
        castSteamAugury(List.of(island, forest, mountain, plains, swamp));

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(mountain, plains, swamp);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @ParameterizedTest
    @CsvSource({"true,true", "true,false", "false,true", "false,false"})
    @DisplayName("An empty pile remains a legal choice with a short library")
    void emptyPileCanBeChosen(boolean firstPileEmpty, boolean chooseFirstPile) {
        Card island = new Island();
        Card forest = new Forest();
        List<Card> cards = List.of(island, forest);
        castSteamAugury(cards);

        harness.handleMultipleCardsChosen(player1,
                firstPileEmpty ? List.of() : List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, chooseFirstPile);

        boolean choseEmptyPile = firstPileEmpty == chooseFirstPile;
        if (choseEmptyPile) {
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(island, forest);
        }
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(island, forest);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(island, forest);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the top five cards are separated and remaining library order is preserved")
    void leavesCardsBelowTopFiveInLibrary() {
        Card island = new Island();
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card plains = new Plains();
        Card swamp = new Swamp();
        Card sixth = new Island();
        Card seventh = new Forest();
        castSteamAugury(List.of(island, forest, mountain, plains, swamp, sixth, seventh));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactlyInAnyOrder(
                        island.getId(), forest.getId(), mountain.getId(), plains.getId(), swamp.getId());
        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mountain, plains, swamp)
                .doesNotContain(sixth, seventh);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth, seventh);
    }

    @Test
    @DisplayName("An empty library resolves without a pile interaction or drawing cards")
    void emptyLibraryResolvesWithoutChoice() {
        castSteamAugury(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
