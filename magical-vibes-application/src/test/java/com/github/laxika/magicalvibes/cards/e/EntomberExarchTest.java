package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.g.GitaxianProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EntomberExarch.class, PhyrexianHulk.class, GitaxianProbe.class, Forest.class})
class EntomberExarchTest extends BaseCardTest {

    @Nested
    @CardUsed({EntomberExarch.class, PhyrexianHulk.class, GitaxianProbe.class})
    @DisplayName("Mode 1: Return target creature card from your graveyard to your hand")
    class ReturnFromGraveyardMode {

        @Test
        @DisplayName("Selects the graveyard target before the trigger resolves and returns it to hand")
        void returnsTargetCreatureFromGraveyardToHand() {
            Card creature = new PhyrexianHulk();
            harness.setGraveyard(player1, List.of(creature));

            castWithGraveyardMode();
            harness.passBothPriorities();

            PendingInteraction.MultiGraveyardChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactly(creature.getId());
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            harness.passBothPriorities();

            harness.assertInHand(player1, "Phyrexian Hulk");
            harness.assertNotInGraveyard(player1, "Phyrexian Hulk");
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Only creature cards in the controller's graveyard are legal targets")
        void onlyOwnCreatureCardsAvailable() {
            Card creature = new PhyrexianHulk();
            Card sorcery = new GitaxianProbe();
            Card opposingCreature = new PhyrexianHulk();
            harness.setGraveyard(player1, List.of(sorcery, creature));
            harness.setGraveyard(player2, List.of(opposingCreature));

            castWithGraveyardMode();
            harness.passBothPriorities();

            PendingInteraction.MultiGraveyardChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validCardIds()).containsExactly(creature.getId());
        }

        @Test
        @DisplayName("A target leaving the graveyard prevents the return without choosing another card")
        void missingTargetDoesNotAllowAnotherChoice() {
            Card target = new PhyrexianHulk();
            Card other = new PhyrexianHulk();
            harness.setGraveyard(player1, List.of(target, other));

            castWithGraveyardMode();
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
            harness.setGraveyard(player1, List.of(other));
            harness.setExile(player1, List.of(target));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        }

        @Test
        @DisplayName("Entomber Exarch enters the battlefield when choosing graveyard mode")
        void exarchEntersBattlefield() {
            harness.setGraveyard(player1, List.of(new PhyrexianHulk()));
            castWithGraveyardMode();
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Entomber Exarch");
        }

        private void castWithGraveyardMode() {
            harness.setHand(player1, List.of(new EntomberExarch()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            harness.castCreature(player1, 0, 0);
        }
    }

    @Test
    @DisplayName("Entering without being cast offers a mode choice")
    void enteringWithoutCastingOffersModeChoice() {
        harness.setGraveyard(player1, List.of(new PhyrexianHulk()));
        harness.setHand(player2, List.of(new GitaxianProbe()));

        harness.enterBattlefieldAndReturn(player1, new EntomberExarch());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Nested
    @CardUsed({EntomberExarch.class, PhyrexianHulk.class, GitaxianProbe.class, Forest.class})
    @DisplayName("Mode 2: Target opponent reveals hand, choose noncreature card to discard")
    class DiscardMode {

        @Test
        @DisplayName("Opponent reveals hand and controller chooses a noncreature card to discard")
        void opponentDiscardsNoncreatureCard() {
            Card sorcery = new GitaxianProbe();
            Card creature = new PhyrexianHulk();
            harness.setHand(player2, List.of(sorcery, creature));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
            // Only sorcery (index 0) should be valid, creature (index 1) should not
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);

            harness.handleCardChosen(player1, 0);

            harness.assertInGraveyard(player2, "Gitaxian Probe");
            assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
            assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Phyrexian Hulk");
        }

        @Test
        @DisplayName("Creature cards are excluded from valid choices")
        void creatureCardsExcluded() {
            Card creature = new PhyrexianHulk();
            Card sorcery = new GitaxianProbe();
            harness.setHand(player2, List.of(creature, sorcery));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Only sorcery (index 1) should be valid
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
        }

        @Test
        @DisplayName("Land cards can be chosen (they are noncreature)")
        void landCardsCanBeChosen() {
            Card land = new Forest();
            Card creature = new PhyrexianHulk();
            harness.setHand(player2, List.of(land, creature));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            // Only land (index 0) should be valid
            assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);
        }

        @Test
        @DisplayName("Empty hand does nothing")
        void emptyHandDoesNothing() {
            harness.setHand(player2, List.of());

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Hand with only creatures results in no valid choices")
        void handWithOnlyCreaturesNoValidChoices() {
            Card creature1 = new PhyrexianHulk();
            Card creature2 = new PhyrexianHulk();
            harness.setHand(player2, List.of(creature1, creature2));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature
            harness.passBothPriorities(); // resolve ETB trigger

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        }

        @Test
        @DisplayName("Entomber Exarch enters the battlefield when choosing discard mode")
        void exarchEntersBattlefield() {
            Card sorcery = new GitaxianProbe();
            harness.setHand(player2, List.of(sorcery));

            castWithDiscardMode();
            harness.passBothPriorities(); // resolve creature

            harness.assertOnBattlefield(player1, "Entomber Exarch");
        }

        @Test
        @DisplayName("The chosen land is discarded and an artifact creature remains in hand")
        void discardsLandAndKeepsArtifactCreature() {
            Card land = new Forest();
            Card creature = new PhyrexianHulk();
            harness.setHand(player2, List.of(land, creature));

            castWithDiscardMode();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);

            assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(land);
            assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        private void castWithDiscardMode() {
            harness.setHand(player1, List.of(new EntomberExarch()));
            harness.addMana(player1, ManaColor.BLACK, 4);
            harness.castCreature(player1, 0, 1, player2.getId());
        }
    }
}
