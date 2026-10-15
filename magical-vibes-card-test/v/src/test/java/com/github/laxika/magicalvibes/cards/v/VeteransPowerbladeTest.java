package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeteransPowerblade.class, VeteranArmorsmith.class, GrizzlyBears.class})
class VeteransPowerbladeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPower() {
        Permanent creature = addCreatureReady(player1, new VeteranArmorsmith());
        Permanent powerblade = harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        powerblade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void soldierEquipAttachesForWhiteMana() {
        Permanent powerblade = harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void soldierEquipRejectsNonSoldier() {
        harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Soldier");
    }

    @Test
    void genericEquipAttachesToNonSoldier() {
        Permanent powerblade = harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void bothEquipAbilitiesRejectOpponentsSoldier() {
        harness.addToBattlefield(player1, new VeteransPowerblade());
        Permanent soldier = addCreatureReady(player2, new VeteranArmorsmith());
        harness.addMana(player1, ManaColor.WHITE, 3);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, soldier.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void bothEquipAbilitiesRejectCombatTiming() {
        harness.addToBattlefield(player1, new VeteransPowerblade());
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, soldier.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sorcery speed");
        }
    }

    @Test
    void soldierEquipRequiresWhiteMana() {
        harness.addToBattlefield(player1, new VeteransPowerblade());
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void reequippingMovesBonusOnlyWhenAbilityResolves() {
        Permanent powerblade = harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        powerblade.setAttachedTo(soldier.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        assertThat(powerblade.getAttachedTo()).isEqualTo(soldier.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void invalidEquipTargetLeavesPreviousAttachmentInPlace() {
        Permanent powerblade = harness.addToBattlefieldAndReturn(player1, new VeteransPowerblade());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent soldier = addCreatureReady(player1, new VeteranArmorsmith());
        powerblade.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, soldier.getId());
        gd.playerBattlefields.get(player1.getId()).remove(soldier);
        gd.playerGraveyards.get(player1.getId()).add(soldier.getCard());
        harness.passBothPriorities();

        assertThat(powerblade.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
