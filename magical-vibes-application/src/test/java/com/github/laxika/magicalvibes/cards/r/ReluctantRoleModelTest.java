package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReluctantRoleModel.class, Assassinate.class, GrizzlyBears.class})
class ReluctantRoleModelTest extends BaseCardTest {

    @Test
    void survivalPutsChosenCounterOnTappedCreature() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a lifelink counter on this creature");
        harness.passBothPriorities();

        assertThat(roleModel.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    void survivalCanPutFlyingOrPlusOneCounter() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a flying counter on this creature");
        harness.passBothPriorities();

        assertThat(roleModel.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalDoesNotTriggerForUntappedCreature() {
        Permanent roleModel = addRoleModel();

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalTriggersOnlyOnceForThePermanentObject() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        roleModel.untap();
        roleModel.tap();
        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherCreatureWithCountersMovesThemToAnyTargetCreatureWhenItDies() {
        addRoleModel();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.FLYING, 1);
        dyingCreature.setCounterCount(CounterType.LIFELINK, 2);
        dyingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(dyingCreature);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.LIFELINK)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void creatureWithoutCountersDoesNotTriggerCounterTransfer() {
        addRoleModel();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(dyingCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ownCountersMoveToAnyTargetCreatureWhenRoleModelDies() {
        Permanent roleModel = addRoleModel();
        roleModel.setCounterCount(CounterType.FLYING, 1);
        roleModel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithAssassinateFromPlayerTwo(roleModel);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addRoleModel() {
        return harness.addToBattlefieldAndReturn(player1, new ReluctantRoleModel());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    private void destroyWithAssassinateFromPlayerTwo(Permanent target) {
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, target.getId(), null);
        harness.passBothPriorities();
    }
}
