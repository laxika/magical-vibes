package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalseOrders.class, SavannahLions.class})
class FalseOrdersTest extends BaseCardTest {

    @Test
    @DisplayName("removes a blocker and can have it block another attacker")
    void removesBlockerAndReassignsIt() {
        Permanent firstAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent secondAttacker = addCreatureReady(player1, new SavannahLions());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();

        castFalseOrders(blocker);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(firstAttacker.isBlockedWithoutBlockers()).isFalse();

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, secondAttacker.getId());

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(secondAttacker.getId());
    }

    @Test
    @DisplayName("can decline to have the removed creature block")
    void canDeclineReassignment() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent blocker = addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.clearPriorityPassed();

        castFalseOrders(blocker);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @DisplayName("can target a defending creature that is not blocking")
    void targetsNonblockingDefendingCreature() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent bystander = addCreatureReady(player2, new SavannahLions());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        castFalseOrders(bystander);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(bystander.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("cannot target an attacking creature or a creature controlled by the attacker")
    void rejectsIllegalTargets() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent defender = addCreatureReady(player2, new SavannahLions());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature defending player controls");

        assertThat(defender.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("reassignment finishes during spell resolution without another priority round")
    void reassignmentResolvesWithSpell() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent bystander = addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0));
        giveSpell();
        gs.declareBlockers(gd, player2, List.of());
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, bystander.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(bystander.isBlocking()).isTrue();
        assertThat(bystander.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("can have a tapped creature block through the effect")
    void tappedCreatureCanBlockThroughEffect() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent bystander = addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0));
        giveSpell();
        gs.declareBlockers(gd, player2, List.of());
        harness.clearPriorityPassed();
        bystander.tap();

        harness.castInstant(player2, 0, bystander.getId());
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMayAbilityChosen(player2, true));

        assertThat(bystander.isTapped()).isTrue();
        assertThat(bystander.isBlocking()).isTrue();
        assertThat(bystander.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("does not unblock an attacker that had another blocker earlier this combat")
    void previouslyMultipleBlockedAttackerStaysBlocked() {
        Permanent attacker = addCreatureReady(player1, new SavannahLions());
        Permanent firstBlocker = addCreatureReady(player2, new SavannahLions());
        Permanent secondBlocker = addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.clearPriorityPassed();

        castFalseOrders(firstBlocker);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMayAbilityChosen(player2, false));
        harness.clearPriorityPassed();
        castFalseOrders(secondBlocker);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handleMayAbilityChosen(player2, false));

        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();
    }

    @Test
    @DisplayName("cannot be cast outside the declare blockers step")
    void rejectsCastingDuringDeclareAttackers() {
        Permanent defender = addCreatureReady(player2, new SavannahLions());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, defender.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void castFalseOrders(Permanent target) {
        giveSpell();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void giveSpell() {
        harness.setHand(player2, List.of(new FalseOrders()));
        harness.addMana(player2, ManaColor.RED, 1);
    }
}
