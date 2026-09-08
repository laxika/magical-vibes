package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PowerTaintTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an enchantment")
    void canEnchantEnchantment() {
        Permanent badMoon = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, badMoon.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a non-enchantment permanent")
    void cannotEnchantNonEnchantment() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Enchanted enchantment's controller loses 2 life when they decline to pay")
    void declinesPaymentAndLosesLife() {
        Permanent badMoon = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        attachPowerTaint(badMoon);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Enchanted enchantment's controller can pay 2 mana to prevent the life loss")
    void paysToPreventLifeLoss() {
        Permanent badMoon = harness.addToBattlefieldAndReturn(player2, new BadMoon());
        attachPowerTaint(badMoon);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cycling discards Power Taint and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Power Taint");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void attachPowerTaint(Permanent enchantment) {
        Permanent powerTaint = new Permanent(new PowerTaint());
        powerTaint.setAttachedTo(enchantment.getId());
        gd.playerBattlefields.get(player1.getId()).add(powerTaint);
    }
}
