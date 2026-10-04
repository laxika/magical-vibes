package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HourOfRevelation;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GateToTheAfterlife.class, DuneBeetle.class, Forest.class, WrathOfGod.class,
        GodPharaohsGift.class, HourOfRevelation.class})
class GateToTheAfterlifeTest extends BaseCardTest {

    @Nested
    @DisplayName("Nontoken creature death trigger")
    @CardUsed({GateToTheAfterlife.class, DuneBeetle.class, Forest.class, WrathOfGod.class,
            HourOfRevelation.class})
    class DeathTrigger {

        @Test
        @DisplayName("Gains 1 life then accepting the may loots (draw then discard)")
        void gainsLifeAndAcceptsLoot() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new DuneBeetle());
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest()));
            int lifeBefore = gd.getLife(player1.getId());

            // Opponent's Wrath destroys the nontoken creature; the artifact survives.
            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Wrath resolves — creature dies, trigger goes on stack

            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities(); // Trigger resolves: gain 1 life, then may loot

            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);

            harness.handleMayAbilityChosen(player1, true); // draw a card
            harness.handleCardChosen(player1, 0);           // discard it

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Forest");
        }

        @Test
        @DisplayName("Gains 1 life; declining the may skips the draw/discard")
        void gainsLifeAndDeclinesLoot() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new DuneBeetle());
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest()));
            int lifeBefore = gd.getLife(player1.getId());

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Wrath resolves — creature dies
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
            harness.addToBattlefield(player1, tokenCard);

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities(); // Wrath resolves — token dies (no trigger)

            assertThat(gd.stack).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        }

        @Test
        void doesNotTriggerForOpponentsCreature() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player2, new DuneBeetle());
            int lifeBefore = gd.getLife(player1.getId());

            harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        }

        @Test
        void triggersForEachCreatureDyingTogether() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new DuneBeetle());
            harness.addToBattlefield(player1, new DuneBeetle());
            int lifeBefore = gd.getLife(player1.getId());

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
            harness.passBothPriorities();

            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        }

        @Test
        void triggersWhenGateAndCreatureDieTogether() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.addToBattlefield(player1, new DuneBeetle());
            int lifeBefore = gd.getLife(player1.getId());

            harness.forceActivePlayer(player2);
            harness.castFromHand(player2, new HourOfRevelation(), "{3}{W}{W}{W}");
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Gate to the Afterlife");
            harness.assertInGraveyard(player1, "Dune Beetle");
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
            harness.handleMayAbilityChosen(player1, false);
        }
    }

    @Nested
    @DisplayName("God-Pharaoh's Gift tutor ability")
    @CardUsed({GateToTheAfterlife.class, DuneBeetle.class, Forest.class, GodPharaohsGift.class})
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

            harness.assertNotOnBattlefield(player1, "Gate to the Afterlife");
            harness.assertInGraveyard(player1, "Gate to the Afterlife");
        }

        @Test
        void putsGiftFromGraveyardOntoBattlefield() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            List<Card> graveyard = creatureCards(6);
            graveyard.add(new GodPharaohsGift());
            harness.setGraveyard(player1, graveyard);
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "God-Pharaoh's Gift");
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(card -> card instanceof GodPharaohsGift);
        }

        @Test
        void putsGiftFromHandOntoBattlefield() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.setHand(player1, List.of(new GodPharaohsGift()));
            harness.setLibrary(player1, List.of(new Forest()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "God-Pharaoh's Gift");
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        }

        @Test
        void putsGiftFromLibraryOntoBattlefield() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new GodPharaohsGift(), new Forest()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);

            harness.assertOnBattlefield(player1, "God-Pharaoh's Gift");
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        }

        @Test
        void canFailToFindGiftInLibrary() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new GodPharaohsGift()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, -1);

            harness.assertNotOnBattlefield(player1, "God-Pharaoh's Gift");
            assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
            harness.assertInGraveyard(player1, "Gate to the Afterlife");
        }

        @Test
        void letsControllerChooseSearchZonesWhenGiftIsInHandAndLibrary() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.setHand(player1, List.of(new GodPharaohsGift()));
            harness.setLibrary(player1, List.of(new GodPharaohsGift()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.activateAbility(player1, 0, 0, null);
            harness.passBothPriorities();

            // The controller can search only the library, leaving the hand copy untouched.
            // An automatic hand selection removes that legal choice.
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertNotOnBattlefield(player1, "God-Pharaoh's Gift");
        }

        @Test
        void noncreatureCardsDoNotMeetActivationRestriction() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            List<Card> graveyard = creatureCards(5);
            graveyard.add(new Forest());
            graveyard.add(new GodPharaohsGift());
            harness.setGraveyard(player1, graveyard);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature cards in your graveyard");
            harness.assertOnBattlefield(player1, "Gate to the Afterlife");
        }

        @Test
        void graveyardRestrictionIsNotRecheckedOnResolution() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.setHand(player1, List.of(new GodPharaohsGift()));
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 0, 0, null);

            harness.setGraveyard(player1, List.of(new GateToTheAfterlife()));
            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "God-Pharaoh's Gift");
        }

        @Test
        void cannotActivateWhenGateIsTapped() {
            harness.addToBattlefieldAndReturn(player1, new GateToTheAfterlife()).tap();
            harness.setGraveyard(player1, creatureCards(6));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertOnBattlefield(player1, "Gate to the Afterlife");
        }

        @Test
        void opponentsGraveyardDoesNotMeetActivationRestriction() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(5));
            harness.setGraveyard(player2, creatureCards(6));
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("creature cards in your graveyard");
            harness.assertOnBattlefield(player1, "Gate to the Afterlife");
        }

        @Test
        void cannotActivateWithoutTwoMana() {
            harness.addToBattlefield(player1, new GateToTheAfterlife());
            harness.setGraveyard(player1, creatureCards(6));
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                    .isInstanceOf(IllegalStateException.class);
            harness.assertOnBattlefield(player1, "Gate to the Afterlife");
        }

        private List<Card> creatureCards(int count) {
            List<Card> cards = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cards.add(new DuneBeetle());
            }
            return cards;
        }
    }
}
