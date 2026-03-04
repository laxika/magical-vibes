package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEquippedEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrainingDroneTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has CantAttackOrBlockUnlessEquippedEffect as static effect")
    void hasCorrectEffect() {
        TrainingDrone card = new TrainingDrone();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackOrBlockUnlessEquippedEffect.class);
    }

    // ===== Cannot attack without equipment =====

    @Test
    @DisplayName("Cannot attack when not equipped")
    void cannotAttackWithoutEquipment() {
        Permanent drone = addDroneReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(droneIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Cannot block without equipment =====

    @Test
    @DisplayName("Cannot block when not equipped")
    void cannotBlockWithoutEquipment() {
        addDroneReady(player2);

        // Set up an attacker on player1's side
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    // ===== Can attack when equipped =====

    @Test
    @DisplayName("Can attack when equipped")
    void canAttackWithEquipment() {
        Permanent drone = addDroneReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(drone.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);
        gs.declareAttackers(gd, player1, List.of(droneIndex));

        assertThat(drone.isAttacking()).isTrue();
    }

    // ===== Can block when equipped =====

    @Test
    @DisplayName("Can block when equipped")
    void canBlockWithEquipment() {
        Permanent drone = addDroneReady(player2);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(drone.getId());

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int droneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(drone);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(droneIndex, 0))))
                .doesNotThrowAnyException();
    }

    // ===== Equipment removed =====

    @Test
    @DisplayName("Cannot attack after equipment is detached")
    void cannotAttackAfterEquipmentDetached() {
        Permanent drone = addDroneReady(player1);
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(drone.getId());

        // Detach the equipment
        scimitar.setAttachedTo(null);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(droneIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Unattached equipment on battlefield doesn't count =====

    @Test
    @DisplayName("Unattached equipment on battlefield does not allow attacking")
    void unattachedEquipmentDoesNotAllowAttack() {
        addDroneReady(player1);
        addScimitarReady(player1); // not attached
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    // ===== Helpers =====

    private Permanent addDroneReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new TrainingDrone());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addScimitarReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new LeoninScimitar());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
