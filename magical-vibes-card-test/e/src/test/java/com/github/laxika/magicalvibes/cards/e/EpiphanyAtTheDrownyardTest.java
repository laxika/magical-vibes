package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CrawlingSensation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpiphanyAtTheDrownyard.class, Island.class, Forest.class, Swamp.class, CrawlingSensation.class})
class EpiphanyAtTheDrownyardTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals X plus one cards and has the controller separate them")
    void revealsXPlusOneAndPromptsController() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(island, forest, swamp));
        cast(2);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                island.getId(), forest.getId(), swamp.getId());
    }

    @Test
    @DisplayName("The opponent chooses which pile goes to hand")
    void opponentChoosesPileForHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(island, forest, swamp));
        cast(2);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("X equals zero still reveals one card")
    void xZeroRevealsOneCard() {
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));
        cast(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(island.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("The opponent can choose an empty pile, sending every revealed card to the graveyard")
    void opponentCanChooseEmptyPile() {
        Card island = new Island();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(island, forest));
        cast(1);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Only the top X plus one cards participate and the opponent can choose the second pile")
    void leavesUnrevealedCardsInLibrary() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(island, forest, swamp));
        cast(1);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island).doesNotContain(forest, swamp);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("A library shorter than X plus one reveals only its remaining cards")
    void shortLibraryRevealsAvailableCards() {
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));
        cast(4);

        harness.handleMultipleCardsChosen(player1, List.of(island.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("An empty library resolves without a pile interaction")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        cast(2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Putting the unchosen land pile into the graveyard triggers Crawling Sensation")
    void unchosenLandPileTriggersCrawlingSensation() {
        harness.addToBattlefield(player1, new CrawlingSensation());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        cast(0);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Insect"))
                .hasSize(1);
    }

    private void cast(int xValue) {
        harness.setHand(player1, List.of(new EpiphanyAtTheDrownyard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castInstantForX(player1, 0, xValue, List.of());
        harness.passBothPriorities();
    }
}
