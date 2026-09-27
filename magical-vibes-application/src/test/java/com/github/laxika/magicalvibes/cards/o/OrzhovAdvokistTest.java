package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovAdvokist.class, GrizzlyBears.class})
class OrzhovAdvokistTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may put two counters on a creature, and accepting restricts that player's attacks")
    void eachPlayerMayPutCountersAndAcceptingRestrictsAttacks() {
        Permanent source = addCreatureReady(player1, new OrzhovAdvokist());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent chosenOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherOpponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, chosenOpponentCreature.getId());

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chosenOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherOpponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining both choices puts no counters and creates no attack restriction")
    void decliningChoicesDoesNothing() {
        addCreatureReady(player1, new OrzhovAdvokist());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("The attack restriction expires at the controller's next turn")
    void restrictionExpiresAtControllerNextTurn() {
        addCreatureReady(player1, new OrzhovAdvokist());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        declareAttackers(player2, List.of(0));
    }

    private void resolveTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
