package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokWindwalker.class, EnsouledScimitar.class, AuriokChampion.class})
class AuriokWindwalkerTest extends BaseCardTest {

    @Test
    void attachesTargetEquipmentToTargetCreature() {
        Permanent windwalker = addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new AuriokChampion());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(windwalker.isTapped()).isTrue();
    }

    @Test
    void canMoveEquipmentBetweenCreatures() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent firstCreature = addCreatureReady(player1, new AuriokChampion());
        Permanent secondCreature = addCreatureReady(player1, new AuriokChampion());
        equipment.setAttachedTo(firstCreature.getId());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(secondCreature.getId());
    }

    @Test
    void cannotTargetAnOpponentEquipment() {
        addReadyWindwalker(player1);
        Permanent opponentEquipment = addEquipment(player2);
        Permanent creature = addCreatureReady(player1, new AuriokChampion());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentEquipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent opponentCreature = addCreatureReady(player2, new AuriokChampion());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(equipment.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANonEquipmentAsTheEquipmentTarget() {
        addReadyWindwalker(player1);
        Permanent nonEquipment = addCreatureReady(player1, new AuriokChampion());
        Permanent creature = addCreatureReady(player1, new AuriokChampion());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(nonEquipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWindwalker(Player player) {
        return addCreatureReady(player, new AuriokWindwalker());
    }

    @Test
    void attachingToTheSameCreatureDoesNotChangeEquipmentTimestamp() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new AuriokChampion());
        equipment.setAttachedTo(creature.getId());
        equipment.setTimestamp(gd.nextTimestamp());
        long originalTimestamp = equipment.getTimestamp();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.getTimestamp()).isEqualTo(originalTimestamp);
    }

    @Test
    void cannotAttachAnimatedEquipment() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new AuriokChampion());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(equipment.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new AuriokWindwalker());
        Permanent equipment = addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new AuriokChampion());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(equipment.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANonCreatureAsTheCreatureTarget() {
        addReadyWindwalker(player1);
        Permanent equipment = addEquipment(player1);
        Permanent otherEquipment = addEquipment(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(equipment.getId(), otherEquipment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEquipment(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new EnsouledScimitar());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
