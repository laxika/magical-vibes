package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScaldingTongsTest extends BaseCardTest {

    private List<Card> handOf(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> (Card) new GrizzlyBears())
                .toList();
    }

    @Test
    @DisplayName("Upkeep trigger deals 1 damage to the chosen opponent with three cards in hand")
    void dealsOneDamageToOpponent() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(3));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No trigger with four cards in hand — intervening if fails at trigger time")
    void doesNotTriggerWithFourCardsInHand() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(4));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep trigger can deal its damage to an opponent's planeswalker")
    void dealsOneDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(3));
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only the opponent is offered — creatures and the controller are illegal targets")
    void offersOnlyOpponentsAndPlaneswalkers() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(1));
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(player2.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(hawk.getId(), player1.getId());
    }
}
