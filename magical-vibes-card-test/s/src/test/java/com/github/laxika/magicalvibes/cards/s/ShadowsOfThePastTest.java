package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShadowsOfThePast.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class ShadowsOfThePastTest extends BaseCardTest {

    @Nested
    @DisplayName("Creature death trigger")
    @CardUsed({ShadowsOfThePast.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
    class DeathTrigger {

        @Test
        @DisplayName("A creature dying puts a scry trigger on the stack that resolves into the scry choice")
        void ownCreatureDeathScries() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0);

            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities(); // trigger resolves into the scry

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        }

        @Test
        @DisplayName("An opponent's creature dying also triggers the scry")
        void opponentCreatureDeathScries() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setLibrary(player1, List.of(new Forest()));

            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        }

        @Test
        void canKeepScriedCardOnTop() {
            Card top = new Forest();
            Card next = new Forest();
            harness.setLibrary(player1, List.of(top, next));
            resolveSingleDeathTrigger();

            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));

            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void canPutScriedCardOnBottom() {
            Card top = new Forest();
            Card next = new Forest();
            harness.setLibrary(player1, List.of(top, next));
            resolveSingleDeathTrigger();

            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        void simultaneousDeathsProduceSeparateScryChoices() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Card first = new Forest();
            Card second = new Forest();
            harness.setLibrary(player1, List.of(first, second));
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.castAndResolveSorcery(player2, 0, 0);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                    .containsExactly(first);
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                    .containsExactly(second);
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void deathWithEmptyLibraryFinishesWithoutAChoice() {
            harness.setLibrary(player1, List.of());
            resolveSingleDeathTrigger();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        }

        private void resolveSingleDeathTrigger() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);
            harness.castAndResolveSorcery(player2, 0, 0);
            harness.passBothPriorities();
        }
    }

    @Nested
    @DisplayName("Drain ability")
    @CardUsed({ShadowsOfThePast.class, GrizzlyBears.class, Forest.class})
    class DrainAbility {

        @Test
        @DisplayName("Cannot activate with fewer than four creature cards in the graveyard")
        void cannotActivateWithoutFourCreatureCards() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.setGraveyard(player1, creatureCards(3));
            harness.addMana(player1, ManaColor.BLACK, 5);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature cards in your graveyard");
        }

        @Test
        @DisplayName("With four creature cards in the graveyard the opponent loses 2 life and the controller gains 2")
        void drainsWithFourCreatureCards() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.setGraveyard(player1, creatureCards(4));
            harness.addMana(player1, ManaColor.BLACK, 5);

            int myLife = gd.getLife(player1.getId());
            int theirLife = gd.getLife(player2.getId());

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            assertThat(gd.getLife(player2.getId())).isEqualTo(theirLife - 2);
            assertThat(gd.getLife(player1.getId())).isEqualTo(myLife + 2);
        }

        @Test
        void noncreatureCardsAndOpponentsGraveyardDoNotMeetTheRestriction() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            List<Card> ownGraveyard = creatureCards(3);
            ownGraveyard.add(new Forest());
            harness.setGraveyard(player1, ownGraveyard);
            harness.setGraveyard(player2, creatureCards(4));
            harness.addMana(player1, ManaColor.BLACK, 5);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature cards in your graveyard");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        void graveyardRestrictionIsNotRecheckedOnResolution() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.setGraveyard(player1, creatureCards(4));
            harness.addMana(player1, ManaColor.BLACK, 5);
            int myLife = gd.getLife(player1.getId());
            int theirLife = gd.getLife(player2.getId());

            harness.activateAbility(player1, 0, 0, null);
            harness.setGraveyard(player1, List.of());
            harness.passBothPriorities();

            harness.assertLife(player1, myLife + 2);
            harness.assertLife(player2, theirLife - 2);
        }

        @Test
        void canActivateTwiceDuringOpponentsTurn() {
            harness.addToBattlefield(player1, new ShadowsOfThePast());
            harness.setGraveyard(player1, creatureCards(5));
            harness.addMana(player1, ManaColor.BLACK, 10);
            harness.forceActivePlayer(player2);
            int myLife = gd.getLife(player1.getId());
            int theirLife = gd.getLife(player2.getId());

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();
            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertLife(player1, myLife + 4);
            harness.assertLife(player2, theirLife - 4);
        }

        private List<Card> creatureCards(int count) {
            List<Card> cards = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cards.add(new GrizzlyBears());
            }
            return cards;
        }
    }
}
