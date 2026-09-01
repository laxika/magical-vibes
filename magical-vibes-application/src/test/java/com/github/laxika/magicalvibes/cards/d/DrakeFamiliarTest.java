package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakeFamiliar.class, RuleOfLaw.class})
class DrakeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no enchantments")
    void autoSacrificesWithNoEnchantments() {
        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInGraveyard(player1, "Drake Familiar");
    }

    @Test
    @DisplayName("An opponent's enchantment can be returned to its owner's hand")
    void opponentEnchantmentCanBeReturned() {
        harness.addToBattlefield(player2, new RuleOfLaw());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID enchantmentId = findPermanent(player2, "Rule of Law").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(enchantmentId);
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player2, "Rule of Law");
        harness.assertInHand(player2, "Rule of Law");
    }

    @Test
    @DisplayName("Returning a controlled enchantment keeps Drake Familiar")
    void returningEnchantmentKeepsDrakeFamiliar() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID enchantmentId = findPermanent(player1, "Rule of Law").getId();
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertInHand(player1, "Rule of Law");
    }

    @Test
    @DisplayName("Declining to return an enchantment sacrifices Drake Familiar")
    void decliningReturnSacrificesDrakeFamiliar() {
        harness.addToBattlefield(player1, new RuleOfLaw());

        castDrakeFamiliar();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInGraveyard(player1, "Drake Familiar");
        harness.assertOnBattlefield(player1, "Rule of Law");
    }

    private void castDrakeFamiliar() {
        harness.setHand(player1, List.of(new DrakeFamiliar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
