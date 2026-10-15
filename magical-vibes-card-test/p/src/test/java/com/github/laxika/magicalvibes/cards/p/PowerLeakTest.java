package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PowerLeak.class, Crusade.class, GrizzlyBears.class, Island.class, Skullcrack.class})
class PowerLeakTest extends BaseCardTest {

    @Test
    @DisplayName("Power Leak can enchant an enchantment")
    void canEnchantEnchantment() {
        Permanent enchantment = addEnchantment(player2);
        harness.setHand(player1, List.of(new PowerLeak()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof PowerLeak
                        && enchantment.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Power Leak cannot enchant a creature")
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PowerLeak()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }

    @Test
    @DisplayName("Enchanted enchantment's controller with no mana takes the full 2 damage")
    void noManaTakesFullDamage() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Paying 1 mana prevents 1 damage, so the controller takes 1")
    void paysOneTakesOne() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities(); // resolve trigger -> prompts for payment
        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Paying 2 mana prevents all 2 damage")
    void paysTwoTakesNone() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Paying 0 mana takes the full 2 damage and spends nothing")
    void paysZeroTakesFull() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Payment prompt allows all available mana")
    void promptAllowsAllAvailableMana() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passBothPriorities(); // resolve trigger -> prompt

        PendingInteraction.XValueChoice ctx =
                (PendingInteraction.XValueChoice) gd.interaction.activeInteraction();
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player2.getId());
        assertThat(ctx.maxValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Controller can tap an untapped Island while choosing the payment")
    void canTapUntappedLandForPayment() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        int islandIndex = gd.playerBattlefields.get(player2.getId()).indexOf(island);
        gs.tapPermanent(gd, player2, islandIndex, null);

        assertThat(island.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.handleXValueChosen(player2, 1);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Artifact-only mana cannot pay Power Leak's generic payment")
    void restrictedManaCannotPay() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerManaPools.get(player2.getId()).addArtifactOnlyColorless(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Power Leak does NOT trigger during the aura controller's own upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Paying more than 2 mana spends the chosen amount and prevents all damage")
    void canPayMoreThanDamageAmount() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 5);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Power Leak triggers on its controller's upkeep when enchanting their enchantment")
    void triggersForOwnEnchantment() {
        Permanent enchantment = addEnchantment(player1);
        attachPowerLeak(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Paying mana cannot prevent Power Leak damage after Skullcrack")
    void paymentCannotPreventUnpreventableDamage() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);
        advanceToUpkeep(player2);
        harness.setHand(player1, List.of(new Skullcrack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passBothPriorities();
        harness.handleXValueChosen(player2, 2);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    private void attachPowerLeak(Permanent enchantment) {
        Permanent powerLeak = harness.addToBattlefieldAndReturn(player1, new PowerLeak());
        powerLeak.setAttachedTo(enchantment.getId());
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Crusade());
    }
}
