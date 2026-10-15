package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BludgeonBrawl;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainingDrone.class, GrizzlyBears.class, LeoninScimitar.class,
        BludgeonBrawl.class, IchorWellspring.class})
class TrainingDroneTest extends BaseCardTest {


    @Test
    @DisplayName("Cannot attack when not equipped")
    void cannotAttackWithoutEquipment() {
        Permanent drone = addDroneReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(droneIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }


    @Test
    @DisplayName("Cannot block when not equipped")
    void cannotBlockWithoutEquipment() {
        addDroneReady(player2);

        // Set up an attacker on player1's side
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }


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
        harness.beginAttackerDeclarationInput();

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);
        gs.declareAttackers(gd, player1, List.of(droneIndex));

        assertThat(drone.isAttacking()).isTrue();
    }


    @Test
    @DisplayName("Can block when equipped")
    void canBlockWithEquipment() {
        Permanent drone = addDroneReady(player2);
        Permanent scimitar = addScimitarReady(player2);
        scimitar.setAttachedTo(drone.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int droneIndex = gd.playerBattlefields.get(player2.getId()).indexOf(drone);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(droneIndex, 0))))
                .doesNotThrowAnyException();
    }


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
        harness.beginAttackerDeclarationInput();

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(droneIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }


    @Test
    @DisplayName("Unattached equipment on battlefield does not allow attacking")
    void unattachedEquipmentDoesNotAllowAttack() {
        addDroneReady(player1);
        addScimitarReady(player1); // not attached
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }


    @Test
    void canAttackWithArtifactMadeEquipment() {
        Permanent drone = addDroneReady(player1);
        harness.addToBattlefield(player1, new BludgeonBrawl());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new IchorWellspring());
        equipment.setAttachedTo(drone.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(drone.isAttacking()).isTrue();
    }

    @Test
    void canBlockWithArtifactMadeEquipment() {
        Permanent drone = addDroneReady(player2);
        harness.addToBattlefield(player2, new BludgeonBrawl());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new IchorWellspring());
        equipment.setAttachedTo(drone.getId());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    private Permanent addDroneReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TrainingDrone());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addScimitarReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LeoninScimitar());
        perm.setSummoningSick(false);
        return perm;
    }
}
