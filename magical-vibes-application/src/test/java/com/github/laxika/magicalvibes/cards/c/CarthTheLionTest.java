package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarthTheLion.class, AjaniGoldmane.class, GrizzlyBears.class, VorinclexMonstrousRaider.class})
class CarthTheLionTest extends BaseCardTest {

    @Test
    @DisplayName("When Carth enters, it offers a planeswalker from the top seven cards")
    void entersAndSearchesForPlaneswalker() {
        AjaniGoldmane ajani = new AjaniGoldmane();
        List<Card> topCards = List.of(ajani, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);

        harness.enterBattlefieldAndReturn(player1, new CarthTheLion());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ajani.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(ajani.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(ajani);
    }

    @Test
    @DisplayName("When a planeswalker you control dies, Carth offers a planeswalker from the top seven")
    void planeswalkerDeathTriggersSearch() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajaniPermanent = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajaniPermanent.setCounterCount(CounterType.LOYALTY, 0);
        ajaniPermanent.setSummoningSick(false);

        AjaniGoldmane ajaniCard = new AjaniGoldmane();
        harness.setLibrary(player1, List.of(ajaniCard, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ajaniCard.getId());
    }

    @Test
    @DisplayName("A creature dying does not trigger Carth's planeswalker death ability")
    void creatureDeathDoesNotTriggerSearch() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);
        List<Card> topCards = List.of(new AjaniGoldmane(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, topCards);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(topCards);
    }

    @Test
    @DisplayName("Adds one loyalty counter to the cost of loyalty abilities you activate")
    void increasesLoyaltyAbilityCost() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        ajani.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void choosesOnlyOnePlaneswalkerFromTheTopSeven() {
        AjaniGoldmane chosen = new AjaniGoldmane();
        AjaniGoldmane unchosen = new AjaniGoldmane();
        AjaniGoldmane eighthCard = new AjaniGoldmane();
        List<Card> rest = List.of(unchosen, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        java.util.ArrayList<Card> library = new java.util.ArrayList<>();
        library.add(chosen);
        library.addAll(rest);
        library.add(eighthCard);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CarthTheLion());
        harness.passBothPriorities();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(rest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CarthTheLion());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclinePlaneswalkerAndBottomOnlyTheTopSeven() {
        AjaniGoldmane ajani = new AjaniGoldmane();
        List<Card> lookedAt = List.of(ajani, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        GrizzlyBears eighthCard = new GrizzlyBears();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(lookedAt);
        library.add(eighthCard);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CarthTheLion());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryWithoutPlaneswalkersIsReturnedToLibrary() {
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new CarthTheLion());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsPlaneswalkerDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 0);
        List<Card> library = List.of(new AjaniGoldmane());
        harness.setLibrary(player1, library);

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player2, "Ajani Goldmane");
    }

    @Test
    void minusOneCostsZeroLoyalty() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void ultimateCanBePaidWithFiveLoyalty() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ajani Goldmane");
        harness.assertOnBattlefield(player1, "Avatar");
    }

    @Test
    void opponentsLoyaltyCostsAreUnaffected() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.passBothPriorities();
    }

    @Test
    void simultaneousDeathOfCarthAndPlaneswalkerStillTriggers() {
        Permanent carth = harness.addToBattlefieldAndReturn(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        carth.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);
        ajani.setCounterCount(CounterType.LOYALTY, 0);
        AjaniGoldmane card = new AjaniGoldmane();
        harness.setLibrary(player1, List.of(card));

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
    }

    @Test
    void vorinclexDoublesTheCombinedPositiveLoyaltyCost() {
        harness.addToBattlefield(player1, new CarthTheLion());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
        harness.passBothPriorities();
    }
}
