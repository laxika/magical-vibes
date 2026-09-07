package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReconnaissanceMission.class, GrizzlyBears.class})
class ReconnaissanceMissionTest extends BaseCardTest {

    @Test
    @DisplayName("A creature dealing combat damage to a player presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        addReconnaissanceMission();
        addReadyAttacker();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw choice draws a card")
    void acceptingDrawsCard() {
        addReconnaissanceMission();
        addReadyAttacker();

        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw choice draws no card")
    void decliningDrawsNoCard() {
        addReconnaissanceMission();
        addReadyAttacker();

        resolveCombatAndTrigger();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Cycling discards Reconnaissance Mission and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ReconnaissanceMission()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reconnaissance Mission");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addReconnaissanceMission() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new ReconnaissanceMission()));
    }

    private void addReadyAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
    }

    private void resolveCombatAndTrigger() {
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
    }
}
