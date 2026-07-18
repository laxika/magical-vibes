package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PowerLeakTest extends BaseCardTest {

    // ===== No mana: full damage, no prompt =====

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

    // ===== Paying mana prevents that much of the 2 damage =====

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

    // ===== The prompt is capped at the damage dealt =====

    @Test
    @DisplayName("Payment prompt is capped at 2 even with more mana available")
    void promptCappedAtTwo() {
        Permanent enchantment = addEnchantment(player2);
        attachPowerLeak(enchantment);

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.passBothPriorities(); // resolve trigger -> prompt

        PendingInteraction.XValueChoice ctx =
                (PendingInteraction.XValueChoice) gd.interaction.activeInteraction();
        assertThat(ctx).isNotNull();
        assertThat(ctx.playerId()).isEqualTo(player2.getId());
        assertThat(ctx.maxValue()).isEqualTo(2);
    }

    // ===== Only fires on the enchanted controller's upkeep =====

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

    // ===== Helpers =====

    private void attachPowerLeak(Permanent enchantment) {
        Permanent powerLeak = new Permanent(new PowerLeak());
        powerLeak.setAttachedTo(enchantment.getId());
        gd.playerBattlefields.get(player1.getId()).add(powerLeak);
    }

    private Permanent addEnchantment(Player player) {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
