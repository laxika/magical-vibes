package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.l.LavaspurBoots;
import com.github.laxika.magicalvibes.cards.m.MobileHomestead;
import com.github.laxika.magicalvibes.cards.s.StopCold;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneLastJob.class, ArmoredArmadillo.class, TrainedArynx.class,
        StopCold.class, LavaspurBoots.class, MobileHomestead.class})
class OneLastJobTest extends BaseCardTest {

    @Test
    void returnsTargetCreature() {
        Card creature = new ArmoredArmadillo();
        addToGraveyard(player1, creature);

        cast(new int[]{0}, List.of(creature.getId()), 5);

        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertNotInGraveyard(player1, creature.getName());
    }

    @Test
    void returnsTargetMountOrVehicle() {
        Card mount = new TrainedArynx();
        addToGraveyard(player1, mount);

        cast(new int[]{1}, List.of(mount.getId()), 4);

        harness.assertOnBattlefield(player1, mount.getName());
    }

    @Test
    void returnsAuraAttachedToControlledCreature() {
        Card aura = new StopCold();
        addToGraveyard(player1, aura);
        Permanent creature = addCreatureReady(player1, new ArmoredArmadillo());

        cast(new int[]{2}, List.of(aura.getId()), 4);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, aura.getName()).getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void returnsEquipmentAttachedToControlledCreature() {
        Card equipment = new LavaspurBoots();
        addToGraveyard(player1, equipment);
        Permanent creature = addCreatureReady(player1, new ArmoredArmadillo());

        cast(new int[]{2}, List.of(equipment.getId()), 4);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, equipment.getName()).getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void returnedAuraTriggersItsEntersAbility() {
        Card aura = new StopCold();
        harness.setGraveyard(player1, List.of(aura));
        Permanent creature = addCreatureReady(player1, new ArmoredArmadillo());

        cast(new int[]{2}, List.of(aura.getId()), 4);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void resolvesMultipleModesWithSeparateGraveyardTargets() {
        Card creature = new ArmoredArmadillo();
        Card mount = new TrainedArynx();
        addToGraveyard(player1, creature, mount);

        cast(new int[]{0, 1}, List.of(creature.getId(), mount.getId()), 6);

        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertOnBattlefield(player1, mount.getName());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void rejectsNonMountOrVehicleForSecondMode() {
        Card creature = new ArmoredArmadillo();
        addToGraveyard(player1, creature);

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsVehicleWithSecondMode() {
        Card vehicle = new MobileHomestead();
        harness.setGraveyard(player1, List.of(vehicle));

        cast(new int[]{1}, List.of(vehicle.getId()), 4);

        harness.assertOnBattlefield(player1, vehicle.getName());
        harness.assertNotInGraveyard(player1, vehicle.getName());
    }

    @Test
    void returnsEquipmentUnattachedWhenControllerHasNoCreatures() {
        Card equipment = new LavaspurBoots();
        harness.setGraveyard(player1, List.of(equipment));
        addCreatureReady(player2, new ArmoredArmadillo());

        cast(new int[]{2}, List.of(equipment.getId()), 4);

        harness.assertOnBattlefield(player1, equipment.getName());
        harness.assertNotInGraveyard(player1, equipment.getName());
        assertThat(findPermanent(player1, equipment.getName()).getAttachedTo()).isNull();
    }

    @Test
    void leavesAuraInGraveyardWhenControllerHasNoCreatures() {
        Card aura = new StopCold();
        harness.setGraveyard(player1, List.of(aura));
        addCreatureReady(player2, new ArmoredArmadillo());
        harness.addToBattlefield(player1, new MobileHomestead());

        cast(new int[]{2}, List.of(aura.getId()), 4);

        harness.assertInGraveyard(player1, aura.getName());
        harness.assertNotOnBattlefield(player1, aura.getName());
    }

    @Test
    void attachesEquipmentToCreatureReturnedByEarlierMode() {
        Card creature = new ArmoredArmadillo();
        Card equipment = new LavaspurBoots();
        harness.setGraveyard(player1, List.of(creature, equipment));

        cast(new int[]{0, 2}, List.of(creature.getId(), equipment.getId()), 6);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, creature.getName()));

        assertThat(findPermanent(player1, equipment.getName()).getAttachedTo())
                .isEqualTo(harness.getPermanentId(player1, creature.getName()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void returnsAllThreeTargetsAndAttachesAuraToReturnedMount() {
        Card creature = new ArmoredArmadillo();
        Card mount = new TrainedArynx();
        Card aura = new StopCold();
        harness.setGraveyard(player1, List.of(creature, mount, aura));

        cast(new int[]{0, 1, 2}, List.of(creature.getId(), mount.getId(), aura.getId()), 7);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, mount.getName()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertOnBattlefield(player1, mount.getName());
        assertThat(findPermanent(player1, aura.getName()).getAttachedTo())
                .isEqualTo(harness.getPermanentId(player1, mount.getName()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void permitsSameMountTargetForFirstTwoModesAndReturnsItOnlyOnce() {
        Card mount = new TrainedArynx();
        harness.setGraveyard(player1, List.of(mount));

        cast(new int[]{0, 1}, List.of(mount.getId(), mount.getId()), 6);

        assertThat(countPermanents(player1, mount.getName())).isEqualTo(1);
        harness.assertNotInGraveyard(player1, mount.getName());
    }

    @Test
    void returnedVehicleCannotServeAsCreatureForEquipmentAttachment() {
        Card vehicle = new MobileHomestead();
        Card equipment = new LavaspurBoots();
        harness.setGraveyard(player1, List.of(vehicle, equipment));

        cast(new int[]{1, 2}, List.of(vehicle.getId(), equipment.getId()), 5);

        harness.assertOnBattlefield(player1, vehicle.getName());
        harness.assertOnBattlefield(player1, equipment.getName());
        assertThat(findPermanent(player1, equipment.getName()).getAttachedTo()).isNull();
    }

    @Test
    void rejectsCreatureInOpponentsGraveyard() {
        Card creature = new ArmoredArmadillo();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId()), 5))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureForAuraOrEquipmentMode() {
        Card creature = new ArmoredArmadillo();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> cast(new int[]{2}, List.of(creature.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresAdditionalManaForSelectedModes() {
        Card creature = new ArmoredArmadillo();
        Card mount = new TrainedArynx();
        harness.setGraveyard(player1, List.of(creature, mount));

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(creature.getId(), mount.getId()), 5))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<UUID> targetIds, int totalMana) {
        harness.setHand(player1, List.of(new OneLastJob()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targetIds, List.of());
        harness.passBothPriorities();
    }

    private void addToGraveyard(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        harness.setGraveyard(player, List.of(cards));
    }

}
