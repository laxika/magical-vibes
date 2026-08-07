package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowsOfThePastTest extends BaseCardTest {

    @Nested
    @DisplayName("Creature death trigger")
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

            harness.getGameService().playCard(gd, player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves, bear dies, trigger goes on stack

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

            harness.getGameService().playCard(gd, player2, 0, 0, null, null);
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        }
    }

    @Nested
    @DisplayName("Drain ability")
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

        private List<Card> creatureCards(int count) {
            List<Card> cards = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cards.add(new GrizzlyBears());
            }
            return cards;
        }
    }
}
