package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.o.OpalCaryatid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerTaint.class, OpalCaryatid.class, GorillaWarrior.class})
class PowerTaintTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an enchantment")
    void canEnchantEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalCaryatid());
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, enchantment.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving attaches Power Taint to the targeted enchantment")
    void resolvingAttachesToTargetedEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalCaryatid());
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PowerTaint
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-enchantment permanent")
    void cannotEnchantNonEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Enchanted enchantment's controller loses 2 life when they decline to pay")
    void declinesPaymentAndLosesLife() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalCaryatid());
        attachPowerTaint(enchantment);
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
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalCaryatid());
        attachPowerTaint(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Power Taint does not trigger during its controller's upkeep")
    void doesNotTriggerDuringAuraControllersUpkeep() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new OpalCaryatid());
        attachPowerTaint(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cycling discards Power Taint and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Power Taint");
        harness.assertInHand(player1, "Gorilla Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated without two generic mana")
    void cyclingRequiresTwoGenericMana() {
        harness.setHand(player1, List.of(new PowerTaint()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Power Taint");
        harness.assertNotInGraveyard(player1, "Power Taint");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private void attachPowerTaint(Permanent enchantment) {
        Permanent powerTaint = harness.addToBattlefieldAndReturn(player1, new PowerTaint());
        powerTaint.setAttachedTo(enchantment.getId());
    }
}
