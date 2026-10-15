package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlersHelm.class, GrizzlyBears.class, WallOfFire.class})
class ProwlersHelmTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature cannot be blocked by a non-Wall creature")
    void equippedCreatureCannotBeBlockedByNonWall() {
        Permanent attacker = addAttackingCreature();
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Equipped creature can be blocked by a Wall")
    void equippedCreatureCanBeBlockedByWall() {
        Permanent attacker = addAttackingCreature();
        Permanent helm = addHelmReady(player1);
        helm.setAttachedTo(attacker.getId());
        Permanent wall = addCreatureReady(player2, new WallOfFire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches Prowler's Helm to a creature you control")
    void equipAbilityAttachesHelm() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addHelmReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new ProwlersHelm());
    }

    @Test
    @DisplayName("An unattached Helm does not restrict blocking")
    void unattachedHelmDoesNotRestrictBlocking() {
        addAttackingCreature();
        addHelmReady(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Re-equipping moves the blocking restriction to the new creature")
    void reEquippingMovesBlockingRestriction() {
        Permanent helm = addHelmReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        helm.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(replacement.getId());
        original.setAttacking(true);
        replacement.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helm.getAttachedTo()).isNull();
    }
}
