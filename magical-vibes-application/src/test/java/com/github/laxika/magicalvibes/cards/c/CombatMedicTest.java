package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombatMedicTest extends BaseCardTest {

    private void addMedicReady() {
        harness.addToBattlefield(player1, new CombatMedic());
        Permanent medic = findPermanent(player1, "Combat Medic");
        medic.setSummoningSick(false);
    }

    private void addActivationManaAndShock() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
    }

    @Test
    @DisplayName("Prevents 1 damage to a target creature")
    void preventsDamageToCreature() {
        addMedicReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationManaAndShock();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Prevents 1 damage to a target player")
    void preventsDamageToPlayer() {
        addMedicReady();
        addActivationManaAndShock();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
