package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChandraFlamecaller;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfJace.class, Forest.class, GideonOfTheTrials.class, GrizzlyBears.class, ChandraFlamecaller.class})
class OathOfJaceTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, draws three cards then makes its controller discard two cards")
    void entersDrawsThreeThenDiscardsTwo() {
        Card discardedOne = new GrizzlyBears();
        Card discardedTwo = new GrizzlyBears();
        harness.setHand(player1, List.of(new OathOfJace(), discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(discardedOne.getId(), discardedTwo.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(3)
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("At upkeep, scries for the number of planeswalkers controlled")
    void upkeepScriesForPlaneswalkersControlled() {
        harness.addToBattlefield(player1, new OathOfJace());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("At upkeep, does not scry when no planeswalkers are controlled")
    void upkeepWithNoPlaneswalkersDoesNotScry() {
        harness.addToBattlefield(player1, new OathOfJace());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("The entry trigger can discard cards drawn by that same trigger")
    void canDiscardNewlyDrawnCards() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new OathOfJace(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Planeswalkers entering after the upkeep trigger are counted at resolution")
    void countsPlaneswalkersAtResolution() {
        harness.addToBattlefield(player1, new OathOfJace());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opposingGideon = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        opposingGideon.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Planeswalkers leaving before resolution no longer contribute to scry")
    void doesNotCountPlaneswalkersThatLeft() {
        harness.addToBattlefield(player1, new OathOfJace());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(gideon);
        gd.playerGraveyards.get(player1.getId()).add(gideon.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Oath of Jace does not trigger on its opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new OathOfJace());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Multiple controlled planeswalkers allow ordering and bottoming the scryed cards")
    void scriesTwoAndOrdersLibrary() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new OathOfJace());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 3);
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraFlamecaller());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
