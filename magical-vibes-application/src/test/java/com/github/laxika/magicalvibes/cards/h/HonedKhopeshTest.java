package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.b.BitterbladeWarrior;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonedKhopesh.class, BitterbladeWarrior.class})
class HonedKhopeshTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip ability attaches equipment to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(khopesh.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        khopesh.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature loses boost when equipment is removed")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        khopesh.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(khopesh);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent otherCreature = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        khopesh.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot equip during opponent's turn")
    void cannotEquipDuringOpponentTurn() {
        addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void equipRequiresOneMana() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(khopesh.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player2, new BitterbladeWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(khopesh.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipNoncreature() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent otherKhopesh = addCreatureReady(player1, new HonedKhopesh());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherKhopesh.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(khopesh.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileStackIsNotEmpty() {
        addCreatureReady(player1, new HonedKhopesh());
        Permanent creature = addCreatureReady(player1, new BitterbladeWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
    }

    @Test
    void reequipMovesBoostOnlyWhenAbilityResolves() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent first = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent second = addCreatureReady(player1, new BitterbladeWarrior());
        khopesh.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(khopesh.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(khopesh.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void illegalTargetLeavesEquipmentOnOriginalCreature() {
        Permanent khopesh = addCreatureReady(player1, new HonedKhopesh());
        Permanent first = addCreatureReady(player1, new BitterbladeWarrior());
        Permanent second = addCreatureReady(player1, new BitterbladeWarrior());
        khopesh.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(khopesh.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
