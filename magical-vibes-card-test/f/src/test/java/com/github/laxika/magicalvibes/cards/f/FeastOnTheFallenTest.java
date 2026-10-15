package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeastOnTheFallen.class, RuneclawBear.class})
class FeastOnTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control when an opponent lost life last turn")
    void putsCounterOnControlledCreatureAfterOpponentLostLife() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        harness.addToBattlefield(player1, new RuneclawBear());
        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.addToBattlefield(player2, new RuneclawBear());
        UUID opponentBearsId = harness.getPermanentId(player2, "Runeclaw Bear");

        gd.lifeLostLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bearsId)
                .doesNotContain(opponentBearsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when only the controller lost life last turn")
    void doesNotTriggerWhenControllerLostLife() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        harness.addToBattlefield(player1, new RuneclawBear());
        gd.lifeLostLastTurn.put(player1.getId(), 2);
        advanceToUpkeep(player1);

        Permanent bears = findPermanent(player1, "Runeclaw Bear");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersDuringOpponentsUpkeepEvenAfterTheyRegainedLife() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());
        gd.lifeLostLastTurn.put(player2.getId(), 5);
        harness.setLife(player2, 25);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bear.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForLifeLostOnlyThisTurn() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        gd.lifeLostThisTurn.put(player2.getId(), 3);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPutAbilityOnStackWithoutControlledCreatures() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        harness.addToBattlefield(player2, new RuneclawBear());
        gd.lifeLostLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetDoesNotRedirectCounterToAnotherCreature() {
        harness.addToBattlefield(player1, new FeastOnTheFallen());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        gd.lifeLostLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
