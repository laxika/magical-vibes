package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PheresBandThunderhoof;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoryOfIroas.class, PheresBandThunderhoof.class})
class ArmoryOfIroasTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets a +1/+1 counter when it attacks")
    void equippedCreatureGetsCounterWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter is put on an attacking creature when Armory of Iroas is unattached")
    void unattachedArmoryDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new PheresBandThunderhoof());
        addCreatureReady(player1, new ArmoryOfIroas());

        declareAttackers(player1, List.of(0));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Armory of Iroas"));
    }

    @Test
    @DisplayName("Equip {2} attaches Armory of Iroas to a creature you control")
    void equipAttachesArmory() {
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        Permanent creature = addCreatureReady(player1, new PheresBandThunderhoof());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armory.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void attackCounterStaysWithOriginalAttackerWhenEquipmentMoves() {
        Permanent attacker = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent other = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        armory.setAttachedTo(other.getId());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackCounterStillResolvesWhenEquipmentBecomesUnattached() {
        Permanent attacker = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        armory.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void onlyEquippedAttackerGetsCounter() {
        Permanent attacker = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent other = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackingWithOtherCreatureDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent attacker = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(equipped.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        Permanent creature = addCreatureReady(player2, new PheresBandThunderhoof());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(armory.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void cannotEquipDuringCombat() {
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        Permanent creature = addCreatureReady(player1, new PheresBandThunderhoof());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(armory.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
    @Test
    void equipPaysExactlyTwoGenericMana() {
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        Permanent creature = addCreatureReady(player1, new PheresBandThunderhoof());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(armory.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void attackCounterStillResolvesWhenEquipmentLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new PheresBandThunderhoof());
        Permanent armory = addCreatureReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(armory);
        gd.playerGraveyards.get(player1.getId()).add(armory.getCard());
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
