package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardsPerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrimFloweringTest extends BaseCardTest {

    @Test
    @DisplayName("Grim Flowering has draw per creature card in graveyard effect")
    void hasCorrectEffect() {
        GrimFlowering card = new GrimFlowering();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DrawCardsPerCreatureCardInGraveyardEffect.class);
        DrawCardsPerCreatureCardInGraveyardEffect effect =
                (DrawCardsPerCreatureCardInGraveyardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.cardsPerCreature()).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card for each creature card in controller's graveyard")
    void drawsForEachCreatureCardInGraveyard() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new GrimFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Only counts creature cards in controller's graveyard")
    void onlyCountsCreatureCards() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GrizzlyBears());
        graveyard.add(new GiantGrowth());
        harness.setGraveyard(player1, graveyard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new GrimFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Does not count creature cards in opponents' graveyards")
    void onlyCountsControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new GrimFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when controller has no creature cards in graveyard")
    void drawsNoCardsWithoutCreatureCards() {
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new GrimFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Grim Flowering goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new GrimFlowering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grim Flowering"));
    }
}
