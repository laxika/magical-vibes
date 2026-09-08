package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VaevictisAsmadiTest extends BaseCardTest {

    @Test
    @DisplayName("Each colored ability gives Vaevictis Asmadi +1/+0 until end of turn")
    void coloredAbilitiesBoostSelf() {
        Permanent vaevictis = harness.addToBattlefieldAndReturn(player1, new VaevictisAsmadi());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vaevictis)).isEqualTo(10);
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Vaevictis Asmadi")
    void decliningUpkeepPaymentSacrificesVaevictis() {
        harness.addToBattlefield(player1, new VaevictisAsmadi());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Vaevictis Asmadi");
        harness.assertInGraveyard(player1, "Vaevictis Asmadi");
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps Vaevictis Asmadi")
    void payingUpkeepCostKeepsVaevictis() {
        harness.addToBattlefield(player1, new VaevictisAsmadi());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Vaevictis Asmadi");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
