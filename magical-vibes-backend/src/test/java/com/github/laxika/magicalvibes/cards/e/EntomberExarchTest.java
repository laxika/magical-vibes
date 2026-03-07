package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntomberExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Entomber Exarch has a ChooseOneEffect with two ETB options")
    void hasCorrectEffects() {
        EntomberExarch card = new EntomberExarch();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.options()).hasSize(2);
        assertThat(effect.options().get(0).effect()).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        assertThat(effect.options().get(1).effect()).isInstanceOf(ChooseCardFromTargetHandToDiscardEffect.class);
    }

    @Nested
    @DisplayName("Mode 1: Return target creature card from your graveyard to your hand")
    class ReturnFromGraveyardMode {

        @Test
        @DisplayName("Returns a creature card from graveyard to hand")
        void returnsCreatureFromGraveyardToHand() {
            Card creature = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(creature);

            castWithGraveyardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Player should be prompted to choose a graveyard card
            harness.handleGraveyardCardChosen(player1, 0);

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Only creature cards are available for choice")
        void onlyCreatureCardsAvailable() {
            Card creature = new GrizzlyBears();
            Card instant = new Peek();
            gd.playerGraveyards.get(player1.getId()).add(instant);
            gd.playerGraveyards.get(player1.getId()).add(creature);

            castWithGraveyardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Only creature (index 1) should be valid, instant (index 0) should not
            assertThat(gd.interaction.graveyardChoiceContext().validIndices()).containsExactly(1);
        }

        @Test
        @DisplayName("Empty graveyard does nothing")
        void emptyGraveyardDoesNothing() {
            castWithGraveyardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Entomber Exarch enters the battlefield when choosing graveyard mode")
        void exarchEntersBattlefield() {
            Card creature = new GrizzlyBears();
            gd.playerGraveyards.get(player1.getId()).add(creature);

            castWithGraveyardMode();
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Entomber Exarch"));
        }

        private void castWithGraveyardMode() {
            harness.setHand(player1, List.of(new EntomberExarch()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            harness.castCreature(player1, 0, 0);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target opponent reveals hand, choose noncreature card to discard")
    class DiscardMode {

        @Test
        @DisplayName("Opponent reveals hand and controller chooses a noncreature card to discard")
        void opponentDiscardsNoncreatureCard() {
            Card instant = new Peek();
            Card creature = new GrizzlyBears();
            harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
            assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
            // Only instant (index 0) should be valid, creature (index 1) should not
            assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);

            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Peek"));
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        }

        @Test
        @DisplayName("Creature cards are excluded from valid choices")
        void creatureCardsExcluded() {
            Card creature = new GrizzlyBears();
            Card instant = new Peek();
            harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Only instant (index 1) should be valid
            assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(1);
        }

        @Test
        @DisplayName("Land cards can be chosen (they are noncreature)")
        void landCardsCanBeChosen() {
            Card land = new Forest();
            Card creature = new GrizzlyBears();
            harness.setHand(player2, new ArrayList<>(List.of(land, creature)));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Only land (index 0) should be valid
            assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(0);
        }

        @Test
        @DisplayName("Empty hand does nothing")
        void emptyHandDoesNothing() {
            harness.setHand(player2, List.of());

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.awaitingInputType()).isNull();
        }

        @Test
        @DisplayName("Hand with only creatures results in no valid choices")
        void handWithOnlyCreaturesNoValidChoices() {
            Card creature1 = new GrizzlyBears();
            Card creature2 = new GrizzlyBears();
            harness.setHand(player2, new ArrayList<>(List.of(creature1, creature2)));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.awaitingInputType()).isNull();
            assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Entomber Exarch enters the battlefield when choosing discard mode")
        void exarchEntersBattlefield() {
            Card instant = new Peek();
            harness.setHand(player2, new ArrayList<>(List.of(instant)));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Entomber Exarch"));
        }

        private void castWithDiscardMode() {
            harness.setHand(player1, List.of(new EntomberExarch()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            harness.castCreature(player1, 0, 1, player2.getId());
        }
    }
}
