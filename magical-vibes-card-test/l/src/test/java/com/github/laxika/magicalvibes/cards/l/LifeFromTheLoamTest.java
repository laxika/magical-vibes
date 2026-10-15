package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HuntedTroll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifeFromTheLoam.class, Forest.class, HuntedTroll.class})
class LifeFromTheLoamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three target land cards from the graveyard to hand")
    void returnsTargetLandCardsToHand() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();
        Card creature = new HuntedTroll();
        LifeFromTheLoam loam = new LifeFromTheLoam();
        harness.setGraveyard(player1, List.of(forest1, forest2, forest3, creature));
        harness.setHand(player1, List.of(loam));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest1.getId(), forest2.getId(), forest3.getId());
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest1, forest2, forest3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(creature, loam);
    }

    @Test
    @DisplayName("May dredge three cards instead of drawing")
    void dredgesInsteadOfDrawing() {
        LifeFromTheLoam loam = new LifeFromTheLoam();
        List<Card> milled = List.of(new Forest(), new HuntedTroll(), new Forest());
        harness.setGraveyard(player1, List.of(loam));
        harness.setLibrary(player1, milled);
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(loam);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can return fewer than three target land cards")
    void returnsFewerThanThreeTargetLandCards() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();
        Card creature = new HuntedTroll();
        LifeFromTheLoam loam = new LifeFromTheLoam();
        harness.setGraveyard(player1, List.of(forest1, forest2, forest3, creature));
        harness.setHand(player1, List.of(loam));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1, List.of(forest2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(forest1, forest3, creature, loam);
    }

    @Test
    @DisplayName("Resolves without a target when the graveyard has no land cards")
    void resolvesWithoutLandTargets() {
        Card creature = new HuntedTroll();
        LifeFromTheLoam loam = new LifeFromTheLoam();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(loam));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(creature, loam);
    }

    @Test
    @DisplayName("May decline dredge and draw normally")
    void mayDeclineDredgeAndDrawNormally() {
        LifeFromTheLoam loam = new LifeFromTheLoam();
        Card topCard = new Forest();
        Card nextCard = new HuntedTroll();
        Card thirdCard = new Forest();
        harness.setGraveyard(player1, List.of(loam));
        harness.setLibrary(player1, List.of(topCard, nextCard, thirdCard));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(loam);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("May choose zero targets even when lands are available")
    void mayChooseZeroAvailableLandTargets() {
        Card forest = new Forest();
        Card opposingForest = new Forest();
        LifeFromTheLoam loam = new LifeFromTheLoam();
        harness.setGraveyard(player1, List.of(forest));
        harness.setGraveyard(player2, List.of(opposingForest));
        harness.setHand(player1, List.of(loam));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(forest, loam);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingForest);
    }

    @Test
    @DisplayName("Cannot dredge with fewer than three cards in the library")
    void drawsNormallyWhenLibraryIsTooSmallForDredge() {
        LifeFromTheLoam loam = new LifeFromTheLoam();
        Card topCard = new Forest();
        Card nextCard = new HuntedTroll();
        harness.setGraveyard(player1, List.of(loam));
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(loam);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Dredge mills only three cards and leaves the rest of the library")
    void dredgeLeavesRemainingLibraryCards() {
        LifeFromTheLoam loam = new LifeFromTheLoam();
        List<Card> milled = List.of(new Forest(), new HuntedTroll(), new Forest());
        Card remainingCard = new Forest();
        List<Card> library = new ArrayList<>(milled);
        library.add(remainingCard);
        harness.setGraveyard(player1, List.of(loam));
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(loam);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
