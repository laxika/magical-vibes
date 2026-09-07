package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CraterElemental.class, AirElemental.class, GrizzlyBears.class})
class CraterElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability deals 4 damage to a target creature")
    void sacrificeAbilityDealsDamage() {
        addCreatureReady(player1, new CraterElemental());
        harness.addToBattlefield(player2, new AirElemental());
        Permanent target = findPermanent(player2, "Air Elemental");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Crater Elemental");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void formidableRequiresTotalPowerEight() {
        harness.addToBattlefield(player1, new CraterElemental());
        addGrizzlyBears(3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable ability sets base power to 8 until end of turn")
    void formidableSetsBasePowerUntilEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new CraterElemental());
        addGrizzlyBears(4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(0);
    }

    private void addGrizzlyBears(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
    }
}
