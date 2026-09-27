package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CarthTheLion.class, AjaniGoldmane.class, GrizzlyBears.class})
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
}
