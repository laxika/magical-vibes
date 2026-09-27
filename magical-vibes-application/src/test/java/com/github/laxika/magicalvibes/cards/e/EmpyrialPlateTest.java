package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmpyrialPlate.class, YotianSoldier.class})
class EmpyrialPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each card in the Equipment controller's hand")
    void boostsPerCardInEquipmentControllersHand() {
        Permanent soldier = addCreatureReady(player2, new YotianSoldier());

        Permanent plate = harness.addToBattlefieldAndReturn(player1, new EmpyrialPlate());
        plate.setAttachedTo(soldier.getId());

        harness.setHand(player1, List.of(new YotianSoldier(), new YotianSoldier(), new YotianSoldier()));
        harness.setHand(player2, List.of(new YotianSoldier()));

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(7);
    }

    @Test
    @DisplayName("Boost updates as the Equipment controller's hand changes")
    void boostUpdatesWithHandSize() {
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());

        Permanent plate = harness.addToBattlefieldAndReturn(player1, new EmpyrialPlate());
        plate.setAttachedTo(soldier.getId());

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);

        harness.setHand(player1, List.of(new YotianSoldier(), new YotianSoldier()));
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(3);

        harness.setHand(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip ability attaches Empyrial Plate to a creature")
    void equipAttachesToCreature() {
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new EmpyrialPlate());

        Permanent soldier = addCreatureReady(player1, new YotianSoldier());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(soldier.getId());
    }

    @Test
    @DisplayName("Empyrial Plate's equip ability costs two mana")
    void equipCostsTwoMana() {
        harness.addToBattlefield(player1, new EmpyrialPlate());
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, soldier.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Unattached Empyrial Plate does not boost a creature")
    void unattachedPlateDoesNotBoostCreature() {
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        harness.addToBattlefield(player1, new EmpyrialPlate());
        harness.setHand(player1, List.of(new YotianSoldier(), new YotianSoldier(), new YotianSoldier()));

        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(4);
    }
}
