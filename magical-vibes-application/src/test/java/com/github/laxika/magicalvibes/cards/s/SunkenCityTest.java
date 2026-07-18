package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunkenCityTest extends BaseCardTest {

    private Permanent find(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Declining to pay {U}{U} sacrifices Sunken City")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sunken City"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sunken City"));
    }

    @Test
    @DisplayName("Paying {U}{U} keeps Sunken City on the battlefield")
    void payingKeeps() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sunken City"));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new SunkenCity());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sunken City"));
    }
}
