package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
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

    private Permanent addEquipment(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new EnsouledScimitar());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
