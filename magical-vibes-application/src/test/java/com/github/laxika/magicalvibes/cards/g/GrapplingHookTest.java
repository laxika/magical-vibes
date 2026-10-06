package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrapplingHook.class, StoneworkPuma.class})
class GrapplingHookTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking lets the controller have a target creature block the equipped creature")
    void attackTriggerCanRequireTargetCreatureToBlock() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMustBlockIds()).containsExactly(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger imposes no block requirement")
    void decliningAttackTriggerDoesNothing() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getMustBlockIds()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying four mana equips the Hook to its target")
    void equipAttachesAndGrantsDoubleStrike() {
        Permanent hook = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hook.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Moving the Hook removes double strike from the previous creature")
    void movingHookTransfersDoubleStrike() {
        Permanent first = addCreatureReady(player1, new StoneworkPuma());
        Permanent second = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(first.getId());

        hook.setAttachedTo(second.getId());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The Equipment controller chooses the target when the equipped creature has another controller")
    void equipmentControllerControlsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player2);
        hook.setAttachedTo(creature.getId());
        addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Each Hook on an attacker allows its own target")
    void multipleHooksHaveIndependentTargets() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        addEquipment(player1).setAttachedTo(creature.getId());
        addEquipment(player1).setAttachedTo(creature.getId());
        Permanent firstBlocker = addCreatureReady(player2, new StoneworkPuma());
        Permanent secondBlocker = addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, firstBlocker.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handlePermanentChosen(player1, secondBlocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(firstBlocker.getMustBlockIds()).containsExactly(creature.getId());
        assertThat(secondBlocker.getMustBlockIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("The attack trigger remembers the original attacker after the Hook moves")
    void movingHookDoesNotChangeRequiredAttacker() {
        Permanent attacker = addCreatureReady(player1, new StoneworkPuma());
        Permanent other = addCreatureReady(player1, new StoneworkPuma());
        Permanent hook = addEquipment(player1);
        hook.setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        hook.setAttachedTo(other.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMustBlockIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("An able targeted creature cannot decline to block")
    void ableTargetMustActuallyBlock() {
        Permanent attacker = addCreatureReady(player1, new StoneworkPuma());
        addEquipment(player1).setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("A tapped target is legal but is not required to block while unable")
    void tappedTargetDoesNotPreventEmptyBlockDeclaration() {
        Permanent attacker = addCreatureReady(player1, new StoneworkPuma());
        addEquipment(player1).setAttachedTo(attacker.getId());
        Permanent blocker = addCreatureReady(player2, new StoneworkPuma());
        blocker.tap();
        addCreatureReady(player2, new StoneworkPuma());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    private Permanent addEquipment(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrapplingHook());
    }
}
