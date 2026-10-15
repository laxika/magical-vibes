package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JackalPup;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScaldingTongs.class, JackalPup.class, LilianaVess.class})
class ScaldingTongsTest extends BaseCardTest {

    private List<Card> handOf(int size) {
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(i -> (Card) new JackalPup())
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
    @DisplayName("Intervening if is checked again when the trigger resolves")
    void doesNotDealDamageIfHandGrowsBeforeResolution() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(3));
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
        Permanent jackal = harness.addToBattlefieldAndReturn(player2, new JackalPup());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(player2.getId());
        assertThat(choice.validIds()).doesNotContain(jackal.getId(), player1.getId());
    }

    @Test
    @DisplayName("An empty hand satisfies the upkeep condition regardless of the opponent's hand")
    void dealsDamageWithEmptyHand() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, List.of());
        harness.setHand(player2, handOf(4));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Scalding Tongs does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller's own planeswalker is a legal target")
    void dealsDamageToOwnPlaneswalker() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(3));
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player2.getId(), liliana.getId());
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The condition may become false and true again before resolution")
    void dealsDamageIfHandReturnsToThreeBeforeResolution() {
        harness.addToBattlefield(player1, new ScaldingTongs());
        harness.setHand(player1, handOf(3));
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, handOf(4));
        harness.setHand(player1, handOf(3));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }
}
