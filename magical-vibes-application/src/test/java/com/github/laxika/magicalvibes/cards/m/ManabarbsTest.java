package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Manabarbs.class, Forest.class, Mountain.class, FurnaceOfRath.class, MindStone.class})
class ManabarbsTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Manabarbs puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Manabarbs()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving Manabarbs puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Manabarbs()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Manabarbs");
    }

    // ===== Trigger: controller taps a land =====

    @Test
    @DisplayName("Controller tapping a land takes 1 damage from Manabarbs")
    void controllerTappingLandTakesDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Mountain is at index 1 (Manabarbs at index 0)
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
    }

    // ===== Trigger: opponent taps a land =====

    @Test
    @DisplayName("Opponent tapping a land takes 1 damage from Manabarbs")
    void opponentTappingLandTakesDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    // ===== Multiple land taps =====

    @Test
    @DisplayName("Tapping multiple lands triggers Manabarbs for each one")
    void tappingMultipleLandsTriggersMultipleTimes() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Tap all three Mountains (indices 1, 2, 3)
        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);
        resolveAllTriggers();

        harness.assertLife(player1, 17);
    }

    // ===== Two Manabarbs =====

    @Test
    @DisplayName("Two Manabarbs on the battlefield each trigger for 2 total damage per land tap")
    void twoManabarbsStack() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        // Two Manabarbs each deal 1 damage = 2 total
        harness.assertLife(player2, 18);
    }

    // ===== Manabarbs on different sides =====

    @Test
    @DisplayName("Manabarbs from different players both trigger on the same land tap")
    void manabarbsFromDifferentPlayersBothTrigger() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Player1 taps Mountain (index 1, after their Manabarbs)
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();

        // Both Manabarbs trigger, dealing 2 total damage to player1
        harness.assertLife(player1, 18);
    }

    // ===== Removing Manabarbs stops the trigger =====

    @Test
    @DisplayName("Removing Manabarbs stops the land-tap damage")
    void removingManabarbsStopsDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);

        // Tap first Mountain (index 1) — takes 1 damage
        harness.tapPermanent(player1, 1);
        resolveAllTriggers();
        harness.assertLife(player1, 19);

        // Remove Manabarbs from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Manabarbs"));

        // After removing Manabarbs (was index 0), Mountains are now at indices 0 (tapped) and 1 (untapped)
        // Tap second Mountain (index 1) — no damage since Manabarbs is gone
        harness.tapPermanent(player1, 1);
        harness.assertLife(player1, 19);
    }

    // ===== Interaction with Furnace of Rath =====

    @Test
    @DisplayName("Furnace of Rath doubles Manabarbs damage to 2")
    void furnaceOfRathDoublesManabarbsDamage() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        // 1 damage doubled to 2 by Furnace of Rath
        harness.assertLife(player2, 18);
    }

    // ===== Manabarbs can kill a player =====

    @Test
    @DisplayName("Manabarbs can reduce a player to 0 life")
    void manabarbsCanKillPlayer() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 1);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("Land-tap damage waits for the Manabarbs trigger to resolve")
    void landTapDamageWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);

        harness.assertLife(player2, 20);
        resolveAllTriggers();
        harness.assertLife(player2, 19);
    }

    // ===== Only triggers for lands, not non-land permanents =====

    @Test
    @DisplayName("Tapping a non-land permanent does not trigger Manabarbs")
    void tappingNonLandDoesNotTrigger() {
        // Use MindStone - an artifact with a tap ability to produce mana
        harness.addToBattlefield(player1, new Manabarbs());
        harness.addToBattlefield(player1, new MindStone());
        harness.setLife(player1, 20);

        // Activate MindStone's mana ability (tap: add {1}) — it's an artifact, not a land
        harness.activateAbility(player1, 1, null, null);

        // Should not take damage — MindStone is an artifact, not a land
        harness.assertLife(player1, 20);
    }
}

