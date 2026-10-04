package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarrukCallerOfBeasts.class, GrizzlyBears.class, AngelicWall.class, Forest.class,
        Island.class, Shock.class})
class GarrukCallerOfBeastsTest extends BaseCardTest {

    @Nested
    @DisplayName("+1 ability")
    @CardUsed({GarrukCallerOfBeasts.class, GrizzlyBears.class, AngelicWall.class,
            Forest.class, Island.class, Shock.class})
    class PlusOne {

        @Test
        void emptyLibraryDoesNotCauseDrawLoss() {
            Permanent garruk = addReadyGarruk(player1);
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of());

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
            harness.assertOnBattlefield(player1, "Garruk, Caller of Beasts");
        }

        @Test
        void onlyTopFiveAreRevealedAndRestGoBelowUntouchedCards() {
            addReadyGarruk(player1);
            harness.setHand(player1, List.of());
            Card creature = new AngelicWall();
            Card first = new Forest();
            Card second = new Island();
            Card third = new Shock();
            Card fourth = new Forest();
            Card untouched = new AngelicWall();
            harness.setLibrary(player1, List.of(creature, first, second, third, fourth, untouched));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
            assertThat(gd.playerDecks.get(player1.getId()))
                    .containsExactly(untouched, fourth, third, second, first);
        }

        @Test
        @DisplayName("Reveals top five: creatures to hand, rest to the bottom of the library")
        void creaturesToHandRestToBottom() {
            Permanent garruk = addReadyGarruk(player1);
            harness.setHand(player1, List.of());
            Card bears = new GrizzlyBears();
            Card wall = new AngelicWall();
            Card forest = new Forest();
            Card island = new Island();
            Card shock = new Shock();
            harness.setLibrary(player1, List.of(bears, wall, forest, island, shock));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
            assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(bears, wall);

            // The three noncreature cards go to the bottom in an order the controller picks.
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                    .containsExactlyInAnyOrder(forest, island, shock);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

            assertThat(gd.playerDecks.get(player1.getId()))
                    .containsExactlyInAnyOrder(forest, island, shock);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Reveals with no creatures: everything goes to the bottom")
        void noCreaturesLeavesHandEmpty() {
            addReadyGarruk(player1);
            harness.setHand(player1, List.of());
            harness.setLibrary(player1, List.of(new Forest(), new Island(), new Shock()));

            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    @Nested
    @DisplayName("−3 ability")
    @CardUsed({GarrukCallerOfBeasts.class, GrizzlyBears.class, AngelicWall.class, Forest.class})
    class MinusThree {

        @Test
        void acceptingWithNoEligibleCardDoesNothing() {
            addReadyGarruk(player1);
            Card wall = new AngelicWall();
            Card forest = new Forest();
            harness.setHand(player1, List.of(wall, forest));

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).containsExactly(wall, forest);
            harness.assertNotOnBattlefield(player1, "Angelic Wall");
            assertThat(gd.interaction.activeInteraction()).isNull();
        }

        @Test
        @DisplayName("Puts a green creature card from hand onto the battlefield")
        void putsGreenCreatureFromHand() {
            Permanent garruk = addReadyGarruk(player1);
            harness.setHand(player1, List.of(new GrizzlyBears()));

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).isEmpty();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Declining puts nothing from hand")
        void decliningPutsNothing() {
            Permanent garruk = addReadyGarruk(player1);
            harness.setHand(player1, List.of(new GrizzlyBears()));

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Only offers green creature cards")
        void onlyOffersGreenCreatures() {
            addReadyGarruk(player1);
            harness.setHand(player1, List.of(new GrizzlyBears(), new AngelicWall(), new Forest()));

            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                    .containsExactly(0);
        }
    }

    @Nested
    @DisplayName("−7 emblem")
    @CardUsed({GarrukCallerOfBeasts.class, GrizzlyBears.class, AngelicWall.class, Forest.class, Shock.class})
    class MinusSeven {

        @Test
        void decliningSearchLeavesLibraryUnchangedAndCreatureSpellOnStack() {
            Permanent garruk = addReadyGarruk(player1);
            garruk.setCounterCount(CounterType.LOYALTY, 7);
            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Garruk, Caller of Beasts");
            Card wall = new AngelicWall();
            Card forest = new Forest();
            harness.setLibrary(player1, List.of(wall, forest));
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castCreature(player1, 0);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wall, forest);
            harness.assertNotOnBattlefield(player1, "Angelic Wall");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
        }

        @Test
        @DisplayName("Emblem searches a creature onto the battlefield when a creature spell is cast")
        void emblemSearchesOnCreatureSpell() {
            Permanent garruk = addReadyGarruk(player1);
            garruk.setCounterCount(CounterType.LOYALTY, 7);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();
            assertThat(gd.emblems).hasSize(1);

            Card wall = new AngelicWall();
            harness.setLibrary(player1, List.of(wall));
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            PendingInteraction.LibrarySearch search =
                    gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
            assertThat(search.params().cards()).containsExactly(wall);

            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            harness.assertOnBattlefield(player1, "Angelic Wall");
        }

        @Test
        @DisplayName("Emblem does not trigger on a noncreature spell")
        void emblemIgnoresNoncreatureSpell() {
            Permanent garruk = addReadyGarruk(player1);
            garruk.setCounterCount(CounterType.LOYALTY, 7);

            harness.activateAbility(player1, 0, 2, null, null);
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
        }
    }

    private Permanent addReadyGarruk(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukCallerOfBeasts());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
