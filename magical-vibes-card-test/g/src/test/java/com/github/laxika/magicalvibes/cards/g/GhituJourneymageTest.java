package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.s.ShivanFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhituJourneymage.class, GhituLavarunner.class, ShivanFire.class})
class GhituJourneymageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers when you control another Wizard")
    void etbTriggersWithAnotherWizard() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ghitu Journeymage");
    }

    @Test
    @DisplayName("ETB deals 2 damage to each opponent when another Wizard is controlled")
    void etbDealsDamageWithAnotherWizard() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB deals 2 damage with non-default life totals")
    void etbDealsDamageWithCustomTotals() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("ETB does NOT trigger without another Wizard (only self)")
    void etbDoesNotTriggerWithoutAnotherWizard() {
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger on the stack
        assertThat(gd.stack).isEmpty();

        // Creature is still on the battlefield
        harness.assertOnBattlefield(player1, "Ghitu Journeymage");

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does NOT trigger when opponent controls a Wizard but you don't")
    void etbDoesNotTriggerWithOpponentWizard() {
        harness.addToBattlefield(player2, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        // No ETB trigger
        assertThat(gd.stack).isEmpty();

        // Life totals unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB does nothing if the other Wizard is removed before resolution")
    void etbFizzlesWhenAnotherWizardRemoved() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack

        // Remove the other Wizard before ETB resolves
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> p.getCard().getName().equals("Ghitu Lavarunner"));

        harness.passBothPriorities(); // resolve ETB trigger — condition no longer met

        // Life totals unchanged (ability does nothing)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("controls another matching permanent ability does nothing"));
    }

    @Test
    @DisplayName("Creature enters battlefield even without another Wizard")
    void creatureEntersWithoutAnotherWizard() {
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Ghitu Journeymage");
    }

    @Test
    @DisplayName("Stack is empty after full resolution with another Wizard")
    void stackEmptyAfterResolution() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB triggers with two other Wizards — still deals only 2 damage")
    void etbTriggersWithMultipleWizards() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castGhituJourneymage() {
        harness.castFromHand(player1, new GhituJourneymage(), "{2}{R}");
    }

    @Test
    @DisplayName("The ability still deals damage after Journeymage dies")
    void abilityResolvesAfterSourceDies() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ShivanFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Ghitu Journeymage"));
        harness.assertInGraveyard(player1, "Ghitu Journeymage");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A different Wizard can satisfy the condition at resolution")
    void differentWizardSatisfiesConditionAtResolution() {
        harness.addToBattlefield(player1, new GhituLavarunner());
        castGhituJourneymage();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new ShivanFire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Ghitu Lavarunner"));
        harness.assertInGraveyard(player1, "Ghitu Lavarunner");
        harness.addToBattlefield(player1, new GhituLavarunner());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }
}
