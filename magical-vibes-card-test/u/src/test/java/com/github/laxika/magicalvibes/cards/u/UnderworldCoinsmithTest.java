package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderworldCoinsmith.class, GloriousAnthem.class, GrizzlyBears.class})
class UnderworldCoinsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when Underworld Coinsmith enters")
    void ownEntryTriggers() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new UnderworldCoinsmith(), "{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Gains 1 life when another enchantment enters under your control")
    void anotherEnchantmentEntryTriggers() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new UnderworldCoinsmith());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when a non-enchantment creature enters under your control")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new UnderworldCoinsmith());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Pays 1 life and makes each opponent lose 1 life")
    void activatedAbilityPaysLifeAndDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new UnderworldCoinsmith());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Another enchantment creature triggers each Coinsmith exactly once")
    void anotherCoinsmithTriggersBoth() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new UnderworldCoinsmith());

        harness.castFromHand(player1, new UnderworldCoinsmith(), "{W}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger your constellation ability")
    void opposingEnchantmentDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new UnderworldCoinsmith());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new UnderworldCoinsmith(), "{W}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Life is paid immediately and a summoning-sick Coinsmith can activate repeatedly")
    void repeatedActivationsPayBeforeResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new UnderworldCoinsmith(), "{W}{B}");
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
