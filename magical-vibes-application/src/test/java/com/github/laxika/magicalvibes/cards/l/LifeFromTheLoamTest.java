package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LifeFromTheLoam.class, Forest.class, GrizzlyBears.class})
class LifeFromTheLoamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to three target land cards from the graveyard to hand")
    void returnsTargetLandCardsToHand() {
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        Card forest3 = new Forest();
        Card creature = new GrizzlyBears();
        LifeFromTheLoam loam = new LifeFromTheLoam();
        harness.setGraveyard(player1, List.of(forest1, forest2, forest3, creature));
        harness.setHand(player1, List.of(loam));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest1.getId(), forest2.getId(), forest3.getId());
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
        List<Card> milled = List.of(new Forest(), new GrizzlyBears(), new Forest());
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
}
