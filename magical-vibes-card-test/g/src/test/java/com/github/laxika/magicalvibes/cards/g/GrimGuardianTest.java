package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimGuardian.class, GloriousAnthem.class, GrizzlyBears.class})
class GrimGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when Grim Guardian enters")
    void ownEntryTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new GrimGuardian(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each opponent loses 1 life when another enchantment enters under your control")
    void anotherEnchantmentEntryTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrimGuardian());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment creature enters under your control")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrimGuardian());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's enchantment enters")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrimGuardian());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A second Grim Guardian triggers both Guardians exactly once")
    void secondGuardianTriggersBothGuardians() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrimGuardian());

        harness.castFromHand(player1, new GrimGuardian(), "{2}{B}");
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Constellation resolves after Grim Guardian dies")
    void triggerSurvivesSourceLeavingBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        var guardian = harness.enterBattlefieldAndReturn(player1, new GrimGuardian());
        harness.assertLife(player2, 20);

        guardian.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Grim Guardian");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }
}
