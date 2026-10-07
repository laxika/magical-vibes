package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thumbscrews.class, GrizzlyBears.class, LilianaVess.class, SuntailHawk.class})
class ThumbscrewsTest extends BaseCardTest {

    private List<Card> handOf(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList();
    }

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to the chosen opponent with five cards in hand")
    void dealsOneDamageToOpponent() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Intervening if is checked again when the trigger resolves")
    void doesNotDealDamageIfHandShrinksBeforeResolution() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, handOf(4));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No trigger with four cards in hand — intervening if fails at trigger time")
    void doesNotTriggerWithFourCardsInHand() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(4));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep trigger can deal its damage to an opponent's planeswalker")
    void dealsOneDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep trigger can deal its damage to the controller's planeswalker")
    void dealsOneDamageToControllersPlaneswalker() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only the opponent is offered — creatures and the controller are illegal targets")
    void offersOnlyOpponentsAndPlaneswalkers() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(6));
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(player2.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(hawk.getId(), player1.getId());
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        harness.setHand(player2, handOf(5));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The opponent's large hand cannot satisfy the controller's hand-size condition")
    void opponentsHandDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(4));
        harness.setHand(player2, handOf(7));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Reaching five cards after upkeep begins does not create a trigger")
    void gainingCardsAfterUpkeepBeginsDoesNotTrigger() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(4));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.setHand(player1, handOf(5));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A temporary drop below five cards does not stop damage if the hand recovers before resolution")
    void dealsDamageIfHandRecoversBeforeResolution() {
        harness.addToBattlefield(player1, new Thumbscrews());
        harness.setHand(player1, handOf(5));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, handOf(4));
        harness.setHand(player1, handOf(6));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
