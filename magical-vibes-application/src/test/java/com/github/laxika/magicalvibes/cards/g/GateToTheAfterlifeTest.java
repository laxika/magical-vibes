package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GateToTheAfterlifeTest extends BaseCardTest {

    @Nested
    @DisplayName("Nontoken creature death trigger")
    class DeathTrigger {

        @Test
        @DisplayName("Gains 1 life then accepting the may loots (draw then discard)")
        void gainsLifeAndAcceptsLoot() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest()));
            int lifeBefore = gd.getLife(player1.getId());

            // Opponent's Wrath destroys the nontoken creature; the artifact survives.
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(gd, player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves — bear dies, trigger goes on stack

            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities(); // Trigger resolves: gain 1 life, then may loot

            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);

            harness.handleMayAbilityChosen(player1, true); // draw a card
            harness.handleCardChosen(player1, 0);           // discard it

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Forest"));
        }

        @Test
        @DisplayName("Gains 1 life; declining the may skips the draw/discard")
        void gainsLifeAndDeclinesLoot() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest()));
            int lifeBefore = gd.getLife(player1.getId());

            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(gd, player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves — bear dies
            harness.passBothPriorities(); // Trigger resolves: gain 1 life, then may loot

            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);

            harness.handleMayAbilityChosen(player1, false); // decline the loot

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1); // card was not drawn
        }

        @Test
        @DisplayName("Does not trigger when a token creature dies")
        void doesNotTriggerOnTokenDeath() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            int lifeBefore = gd.getLife(player1.getId());

            Card tokenCard = new Card();
            tokenCard.setName("Zombie");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.BLACK);
            tokenCard.setPower(2);
            tokenCard.setToughness(2);
            gd.playerBattlefields.get(player1.getId()).add(new Permanent(tokenCard));

            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(gd, player2, 0, 0, null, null);
            harness.passBothPriorities(); // Wrath resolves — token dies (no trigger)

            assertThat(gd.stack).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        }
    }

    @Nested
    @DisplayName("God-Pharaoh's Gift tutor ability")
    class TutorAbility {

        @Test
        @DisplayName("Cannot activate with fewer than six creature cards in the graveyard")
        void cannotActivateWithoutSixCreatureCards() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(5));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature cards in your graveyard");
        }

        @Test
        @DisplayName("Activates with six creature cards, sacrificing itself to search")
        void activatesWithSixCreatureCards() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null); // paying {2}, {T}, sacrifice
            harness.passBothPriorities();                 // resolve the search (finds nothing)

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Gate to the Afterlife"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Gate to the Afterlife"));
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
