package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrakeFamiliar.class, DoublingSeason.class})
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
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new DrakeFamiliar()).getId();
        harness.addToBattlefield(player2, new DoublingSeason());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID enchantmentId = findPermanent(player2, "Doubling Season").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(enchantmentId)
                .doesNotContain(creatureId);
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player2, "Doubling Season");
        harness.assertInHand(player2, "Doubling Season");
        harness.assertOnBattlefield(player2, "Drake Familiar");
    }

    @Test
    @DisplayName("Returning a controlled enchantment keeps Drake Familiar")
    void returningEnchantmentKeepsDrakeFamiliar() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castDrakeFamiliar();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID enchantmentId = findPermanent(player1, "Doubling Season").getId();
        harness.handlePermanentChosen(player1, enchantmentId);

        harness.assertOnBattlefield(player1, "Drake Familiar");
        harness.assertNotOnBattlefield(player1, "Doubling Season");
        harness.assertInHand(player1, "Doubling Season");
    }

    @Test
    @DisplayName("Declining to return an enchantment sacrifices Drake Familiar")
    void decliningReturnSacrificesDrakeFamiliar() {
        harness.addToBattlefield(player1, new DoublingSeason());

        castDrakeFamiliar();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drake Familiar");
        harness.assertInGraveyard(player1, "Drake Familiar");
        harness.assertOnBattlefield(player1, "Doubling Season");
    }

    private void castDrakeFamiliar() {
        harness.castFromHand(player1, new DrakeFamiliar(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
