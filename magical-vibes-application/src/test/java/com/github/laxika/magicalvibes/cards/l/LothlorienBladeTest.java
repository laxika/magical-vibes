package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LothlorienBlade.class, LlanowarElves.class, GrizzlyBears.class, EnormousBaloth.class})
class LothlorienBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature deals power damage to a creature defending player controls")
    void equippedCreatureDealsPowerDamageToDefendingCreature() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        elf.setPowerModifier(2);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Elf equip attaches to an Elf")
    void elfEquipAttachesToElf() {
        Permanent blade = addBladeReady(player1);
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(elf.getId());
    }

    @Test
    @DisplayName("Elf equip rejects a non-Elf creature")
    void elfEquipRejectsNonElf() {
        addBladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Elf");
    }

    @Test
    @DisplayName("Generic equip attaches to a non-Elf creature")
    void genericEquipAttachesToNonElf() {
        Permanent blade = addBladeReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void attackDamageStillResolvesAfterBladeLeavesBattlefield() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        elf.setPowerModifier(2);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, blade));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void attackDamageUsesAttackersLastKnownPowerAfterItLeaves() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        elf.setPowerModifier(3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, elf));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void movingBladeDoesNotChangeWhichCreatureDealsAttackDamage() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        elf.setPowerModifier(2);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        blade.setAttachedTo(bears.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void attackDamageUsesPowerAtResolution() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        harness.handlePermanentChosen(player1, target.getId());
        elf.setPowerModifier(3);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void attackTriggerCannotTargetControllersCreature() {
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(elf.getId());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new EnormousBaloth());

        declareAttack(player1, elf);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void bothEquipAbilitiesRejectOpponentsElf() {
        addBladeReady(player1);
        Permanent elf = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, elf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBladeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new LothlorienBlade());
    }

    private void declareAttack(Player player, Permanent attacker) {
        int index = gd.playerBattlefields.get(player.getId()).indexOf(attacker);
        declareAttackers(player, List.of(index));
    }
}
