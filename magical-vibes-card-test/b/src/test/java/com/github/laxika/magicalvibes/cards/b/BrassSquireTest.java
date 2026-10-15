package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrassSquire.class, GrizzlyBears.class, StriderHarness.class})
class BrassSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack with both targets")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Brass Squire");
        assertThat(entry.getTargetIds()).containsExactly(equipment.getId(), creature.getId());
    }

    @Test
    @DisplayName("Resolving ability attaches equipment to target creature")
    void resolvingAbilityAttachesEquipment() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can move equipment from one creature to another")
    void canMoveEquipmentBetweenCreatures() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player1, new GrizzlyBears());

        // First attach to creature1
        equipment.setAttachedTo(creature1.getId());
        assertThat(equipment.getAttachedTo()).isEqualTo(creature1.getId());

        // Use Brass Squire to move to creature2
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature2.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature2.getId());
    }

    @Test
    @DisplayName("Ability cannot attach if equipment leaves battlefield before resolution")
    void doesNotAttachIfEquipmentLeaves() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        // Remove equipment before resolution
        gd.playerBattlefields.get(player1.getId()).remove(equipment);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Equipment is gone so nothing should be attached
    }

    @Test
    @DisplayName("Ability cannot attach if creature leaves battlefield before resolution")
    void doesNotAttachIfCreatureLeaves() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        // Remove creature before resolution
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Can activate at instant speed (during opponent's turn)")
    void worksAtInstantSpeed() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        // Force to opponent's turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player1 should still be able to activate (instant speed)
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Brass Squire");
    }

    @Test
    @DisplayName("Activating the ability taps Brass Squire")
    void activatingTapsSquire() {
        Permanent squire = addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(squire.isTapped()).isFalse();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));

        assertThat(squire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent squire = addCreatureReady(player1, new BrassSquire());
        squire.tap();
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Attaching Equipment to its current host does not refresh its timestamp")
    void attachingToCurrentHostDoesNothing() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(creature.getId());
        equipment.setTimestamp(gd.nextTimestamp());
        long originalTimestamp = equipment.getTimestamp();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.getTimestamp()).isEqualTo(originalTimestamp);
    }

    @Test
    void canAttachEquipmentToBrassSquire() {
        Permanent squire = addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), squire.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(squire.getId());
    }

    @Test
    void abilityResolvesAfterSquireLeaves() {
        Permanent squire = addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(squire);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void cannotTargetOpponentsEquipment() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player2, new StriderHarness());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipmentStaysOnOriginalHostIfTargetChangesController() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent originalHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent newHost = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(originalHost.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), newHost.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(newHost);
        gd.playerBattlefields.get(player2.getId()).add(newHost);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(originalHost.getId());
    }

    @Test
    void cannotMoveEquipmentThatChangesControllerBeforeResolution() {
        addCreatureReady(player1, new BrassSquire());
        Permanent equipment = addCreatureReady(player1, new StriderHarness());
        Permanent originalHost = addCreatureReady(player1, new GrizzlyBears());
        Permanent newHost = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(originalHost.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), newHost.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(originalHost.getId());
    }
}
