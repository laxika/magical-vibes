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

@CardUsed({ReluctantRoleModel.class, Assassinate.class, GrizzlyBears.class, RelentlessAssault.class})
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
    void survivalDoesNotTriggerDuringThirdMainPhase() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void survivalTriggersAgainOnLaterTurns() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        roleModel.tap();
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on this creature");
        harness.passBothPriorities();

        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void survivalDoesNothingIfUntappedBeforeResolution() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();
        advanceToPostcombatMain(player1);
        assertThat(gd.stack).hasSize(1);
        roleModel.untap();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(roleModel.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(roleModel.getCounterCount(CounterType.LIFELINK)).isZero();
        assertThat(roleModel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalDoesNotTriggerDuringOpponentsTurn() {
        Permanent roleModel = addRoleModel();
        roleModel.tap();
        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ownDeathWithoutCountersDoesNotTrigger() {
        Permanent roleModel = addRoleModel();
        destroyWithAssassinateFromPlayerTwo(roleModel);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void counterTransferCanChooseNoTarget() {
        Permanent roleModel = addRoleModel();
        roleModel.setCounterCount(CounterType.FLYING, 1);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWithAssassinateFromPlayerTwo(roleModel);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsCreatureDeathDoesNotTriggerTransfer() {
        addRoleModel();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.FLYING, 1);
        destroyWithAssassinateFromPlayerTwo(dyingCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusOneCountersAreTransferredAndCanKillRecipient() {
        addRoleModel();
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(recipient);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(recipient.getCard());
    }

    @Test
    void simultaneousDeathTriggersForBothRoleModelAndAnotherCreature() {
        Permanent roleModel = addRoleModel();
        roleModel.setCounterCount(CounterType.FLYING, 1);
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dyingCreature.setCounterCount(CounterType.LIFELINK, 2);
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        roleModel.setMarkedDamage(2);
        dyingCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(recipient.getCounterCount(CounterType.LIFELINK)).isEqualTo(2);
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
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    private void destroyWithAssassinateFromPlayerTwo(Permanent target) {
        target.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
