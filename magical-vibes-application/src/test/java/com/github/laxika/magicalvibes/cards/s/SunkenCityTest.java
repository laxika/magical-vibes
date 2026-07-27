package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunkenCityTest extends BaseCardTest {

    private Permanent find(Player player, String name) {
        return findPermanent(player, name);
    }

    // ===== Buffs blue creatures (all controllers) =====

    @Test
    @DisplayName("Own blue creatures get +1/+1")
    void buffsOwnBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent wizard = find(player1, "Fugitive Wizard");

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's blue creatures also get +1/+1")
    void buffsOpponentBlueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent wizard = find(player2, "Fugitive Wizard");

        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblue creatures are unaffected")
    void doesNotBuffNonblueCreatures() {
        harness.addToBattlefield(player1, new SunkenCity());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = find(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Upkeep sacrifice-unless-pay =====

    @Test
    @DisplayName("Declining to pay {U}{U} sacrifices Sunken City")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Sunken City");
        harness.assertInGraveyard(player1, "Sunken City");
    }

    @Test
    @DisplayName("Paying {U}{U} keeps Sunken City on the battlefield")
    void payingKeeps() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sunken City");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sunken City");
    }
}
