package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AbsoluteGrace;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.DarkestHour;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaithHealer.class, AbsoluteGrace.class, DarkestHour.class, CoralMerfolk.class})
class FaithHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an enchantment gains life equal to its mana value")
    void sacrificeEnchantmentGainsItsManaValue() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.addToBattlefield(player1, new AbsoluteGrace());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Absolute Grace");
        harness.assertInGraveyard(player1, "Absolute Grace");
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Choosing an enchantment sacrifices that enchantment and uses its mana value")
    void choosesEnchantmentAndUsesItsManaValue() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.addToBattlefield(player1, new DarkestHour());
        Permanent grace = harness.addToBattlefieldAndReturn(player1, new AbsoluteGrace());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, grace.getId());

        harness.assertNotOnBattlefield(player1, "Absolute Grace");
        harness.assertInGraveyard(player1, "Absolute Grace");
        harness.assertOnBattlefield(player1, "Darkest Hour");
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Cannot activate without an enchantment to sacrifice")
    void requiresEnchantmentToSacrifice() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-enchantment permanent")
    void cannotSacrificeNonEnchantment() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");

        harness.assertOnBattlefield(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's enchantment")
    void cannotSacrificeOpponentsEnchantment() {
        harness.addToBattlefield(player1, new FaithHealer());
        harness.addToBattlefield(player2, new AbsoluteGrace());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching");

        harness.assertOnBattlefield(player2, "Absolute Grace");
    }
}
