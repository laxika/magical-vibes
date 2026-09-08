package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionCharm.class, Forest.class, GrizzlyBears.class, Island.class, Millstone.class})
class VisionCharmTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player mills four cards")
    class MillMode {

        @Test
        @DisplayName("Mills four cards from the targeted player")
        void millsFourCards() {
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            List<Card> deck = gd.playerDecks.get(player2.getId());
            while (deck.size() > 6) {
                deck.removeFirst();
            }
            int deckBefore = deck.size();
            Card first = deck.get(0);
            Card fourth = deck.get(3);

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 4);
            assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, fourth);
        }
    }

    @Nested
    @DisplayName("Mode 1: Lands of chosen type become chosen basic type until end of turn")
    class LandTypeMode {

        @Test
        @DisplayName("Prompts for a land type, then a basic land type")
        void promptsForTwoChoices() {
            harness.addToBattlefield(player1, new Forest());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();

            var first = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(first.context()).isInstanceOf(ChoiceContext.LandsOfTypeBecomeBasicTypeChoice.class);
            assertThat(((ChoiceContext.LandsOfTypeBecomeBasicTypeChoice) first.context()).fromType()).isNull();

            harness.handleListChoice(player1, "FOREST");

            var second = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(second.context()).isInstanceOf(ChoiceContext.LandsOfTypeBecomeBasicTypeChoice.class);
            assertThat(((ChoiceContext.LandsOfTypeBecomeBasicTypeChoice) second.context()).fromType())
                    .isEqualTo(CardSubtype.FOREST);
        }

        @Test
        @CardUsed(Desert.class)
        @DisplayName("Offers nonbasic land types for the first choice")
        void offersNonBasicLandTypesForFirstChoice() {
            harness.addToBattlefield(player1, new Desert());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();

            var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice.options()).contains("DESERT");
        }

        @Test
        @DisplayName("All Forests become Islands until end of turn, including the opponent's")
        void convertsMatchingLandsGlobally() {
            Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
            Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
            Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            castLandModeAndChoose("FOREST", "ISLAND");

            assertThat(ownForest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
            assertThat(opponentForest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
            assertThat(island.getTransientLandTypeOverride()).isNull();
        }

        @Test
        @DisplayName("Converted Forest produces blue mana")
        void convertedForestProducesBlue() {
            harness.addToBattlefield(player1, new Forest());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            castLandModeAndChoose("FOREST", "ISLAND");

            int forestIndex = indexOnBattlefield(player1, "Forest");
            harness.tapPermanent(player1, forestIndex);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        }

        @Test
        @DisplayName("Override clears at end of turn")
        void overrideClearsAtEndOfTurn() {
            Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            castLandModeAndChoose("FOREST", "ISLAND");
            assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

            forest.resetModifiers();

            assertThat(forest.getTransientLandTypeOverride()).isNull();
        }

        private void castLandModeAndChoose(String fromType, String toType) {
            harness.castModalInstant(player1, 0, 1, List.of());
            harness.passBothPriorities();
            harness.handleListChoice(player1, fromType);
            harness.handleListChoice(player1, toType);
        }
    }

    @Nested
    @DisplayName("Mode 2: Target artifact phases out")
    class PhaseOutMode {

        @Test
        @DisplayName("Phases out the targeted artifact")
        void phasesOutArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            harness.castInstant(player1, 0, 2, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            assertThat(gd.phasedOutPermanents.get(player2.getId()))
                    .anyMatch(permanent -> permanent.getId().equals(targetId));
        }

        @Test
        @DisplayName("Cannot target a non-artifact")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new VisionCharm()));
            harness.addMana(player1, ManaColor.BLUE, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, targetId))
                    .hasMessageContaining("Target");
        }
    }

    private int indexOnBattlefield(com.github.laxika.magicalvibes.model.Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("No " + name + " on battlefield");
    }
}
