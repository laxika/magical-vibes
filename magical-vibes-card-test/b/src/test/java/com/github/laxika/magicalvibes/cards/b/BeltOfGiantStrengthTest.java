package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeltOfGiantStrength.class, GrizzlyBears.class, Ornithopter.class})
class BeltOfGiantStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has base power and toughness 10/10")
    void equippedCreatureHasBaseTenTen() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        belt.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    @DisplayName("An unattached Belt does not change a creature's power or toughness")
    void unattachedBeltDoesNotBoostCreature() {
        addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip costs less by the target creature's power")
    void equipCostIsReducedByTargetPower() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Equip reduction uses power rather than toughness")
    void equipReductionUsesPower() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(belt.getAttachedTo()).isNull();
    }

    @Test
    void zeroPowerCreatureRequiresFullEquipCost() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    void negativePowerDoesNotIncreaseEquipCost() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new Ornithopter());
        creature.setPowerModifier(-2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    void countersReduceEquipCostAndRemainAboveTheNewBaseStats() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(13);
    }

    @Test
    void powerAboveTenMakesEquipFreeWithoutAddingMana() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(19);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(19);
    }

    @Test
    void alreadyEquippedCreatureCanBeTargetedForFree() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        belt.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void movingBeltUsesNewTargetsPowerAndRestoresOldCreaturesStats() {
        Permanent belt = addBeltReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new Ornithopter());
        belt.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, oldCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, newCreature)).isEqualTo(10);
    }

    @Test
    void powerChangeAfterActivationDoesNotChangePaidCost() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        creature.setPowerModifier(-2);
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    void vanishedTargetLeavesBeltOnOriginalCreatureWithoutRefund() {
        Permanent belt = addBeltReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent newCreature = addCreatureReady(player1, new GrizzlyBears());
        belt.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(newCreature);
        gd.playerGraveyards.get(player1.getId()).add(newCreature.getCard());
        harness.passBothPriorities();

        assertThat(belt.getAttachedTo()).isEqualTo(oldCreature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(10);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(belt.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(10);
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent belt = addBeltReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(belt.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(8);
    }

    private Permanent addBeltReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BeltOfGiantStrength());
        permanent.setSummoningSick(false);
        return permanent;
    }

}
