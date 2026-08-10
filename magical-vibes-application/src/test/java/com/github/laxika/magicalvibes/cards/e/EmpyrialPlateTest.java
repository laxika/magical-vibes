package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmpyrialPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each card in the Equipment controller's hand")
    void boostsPerCardInEquipmentControllersHand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent plate = new Permanent(new EmpyrialPlate());
        plate.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(plate);

        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost updates as the Equipment controller's hand changes")
    void boostUpdatesWithHandSize() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent plate = new Permanent(new EmpyrialPlate());
        plate.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(plate);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches Empyrial Plate to a creature")
    void equipAttachesToCreature() {
        Permanent plate = new Permanent(new EmpyrialPlate());
        gd.playerBattlefields.get(player1.getId()).add(plate);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(bears.getId());
    }
}
