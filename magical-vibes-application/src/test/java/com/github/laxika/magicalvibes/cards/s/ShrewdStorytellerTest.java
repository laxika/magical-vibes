package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrewdStoryteller.class, GrizzlyBears.class})
class ShrewdStorytellerTest extends BaseCardTest {

    @Test
    void tappedStorytellerPutsCounterOnTargetCreatureAtPostcombatMain() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        storyteller.tap();

        advanceToPostcombatMain(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void untappedStorytellerDoesNotTrigger() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(storyteller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void untappingBeforeResolutionPreventsCounter() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        storyteller.untap();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalTriggersOnlyOnceForThePermanentObject() {
        Permanent storyteller = harness.addToBattlefieldAndReturn(player1, new ShrewdStoryteller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        storyteller.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        storyteller.untap();
        storyteller.tap();
        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
