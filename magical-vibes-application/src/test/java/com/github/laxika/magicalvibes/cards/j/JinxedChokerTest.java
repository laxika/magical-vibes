package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JinxedChokerTest extends BaseCardTest {

    @Test
    @DisplayName("At the controller's end step, an opponent gains control and a charge counter is added")
    void endStepTransfersChokerAndAddsCounter() {
        Permanent choker = harness.addToBattlefieldAndReturn(player1, new JinxedChoker());
        choker.setCounterCount(CounterType.CHARGE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(choker);
        assertThat(choker.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At the controller's upkeep, Jinxed Choker deals damage equal to its charge counters")
    void upkeepDealsDamageEqualToChargeCounters() {
        Permanent choker = harness.addToBattlefieldAndReturn(player2, new JinxedChoker());
        choker.setCounterCount(CounterType.CHARGE, 3);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The activated ability can add or remove a charge counter")
    void activatedAbilityAddsOrRemovesCounter() {
        Permanent choker = harness.addToBattlefieldAndReturn(player1, new JinxedChoker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a charge counter on Jinxed Choker");
        assertThat(choker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Remove a charge counter from Jinxed Choker");
        assertThat(choker.getCounterCount(CounterType.CHARGE)).isZero();
    }
}
