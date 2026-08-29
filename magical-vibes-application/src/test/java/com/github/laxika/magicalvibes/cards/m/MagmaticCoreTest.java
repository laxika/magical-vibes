package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MagmaticCoreTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, distributes damage equal to its age counters among target creatures")
    void distributesDamageAmongTargetCreatures() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 3);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, firstCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        PendingInteraction.XValueChoice allocation =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(allocation).isNotNull();
        assertThat(allocation.minValue()).isEqualTo(1);
        assertThat(allocation.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firstCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondCreature.getCard());
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentEndStep() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());
        core.setCounterCount(CounterType.AGE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep adds an age counter and can be paid")
    void cumulativeUpkeepAddsAgeCounterAndCanBePaid() {
        Permanent core = harness.addToBattlefieldAndReturn(player1, new MagmaticCore());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(core.getCounterCount(CounterType.AGE)).isEqualTo(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(core);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
