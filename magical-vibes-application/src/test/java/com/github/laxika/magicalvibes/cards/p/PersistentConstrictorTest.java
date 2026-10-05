package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PersistentConstrictor.class, GrizzlyBears.class, Murder.class})
class PersistentConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent upkeep causes life loss and puts a -1/-1 counter on their creature")
    void opponentUpkeepTriggersBothEffects() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The optional target can be declined while life loss still happens")
    void optionalTargetCanBeDeclined() {
        addCreatureReady(player1, new PersistentConstrictor());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only the active player's creatures are legal targets")
    void onlyActivePlayersCreatureCanBeTargeted() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentBears.getId()));
    }

    @Test
    @DisplayName("Persist returns Persistent Constrictor with a -1/-1 counter")
    void persistReturnsWithCounter() {
        Permanent constrictor = addCreatureReady(player1, new PersistentConstrictor());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, constrictor.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Persistent Constrictor");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void canDeclineTargetEvenWhenCreatureIsAvailable() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringControllersUpkeep() {
        addCreatureReady(player1, new PersistentConstrictor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void losingOnlyTargetPreventsLifeLossAsWell() {
        addCreatureReady(player1, new PersistentConstrictor());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        advanceToUpkeep(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
    }

    @Test
    void persistDoesNotReturnCreatureThatDiedWithMinusOneCounter() {
        Permanent constrictor = addCreatureReady(player1, new PersistentConstrictor());
        constrictor.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, constrictor.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Persistent Constrictor");
        harness.assertInGraveyard(player1, "Persistent Constrictor");
    }
}
