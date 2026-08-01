package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NivMizzetDracogeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player presents the may-draw choice")
    void combatDamagePresentsMayChoice() {
        Permanent niv = addCreatureReady(player1, new NivMizzetDracogenius());
        niv.setAttacking(true);

        resolveCombatAndMayDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may-draw after combat damage draws a card")
    void acceptingCombatMayDrawsCard() {
        Permanent niv = addCreatureReady(player1, new NivMizzetDracogenius());
        niv.setAttacking(true);

        resolveCombatAndMayDraw();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Declining the may-draw after combat damage does not draw")
    void decliningCombatMayDoesNotDraw() {
        Permanent niv = addCreatureReady(player1, new NivMizzetDracogenius());
        niv.setAttacking(true);

        resolveCombatAndMayDraw();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("{U}{R} deals 1 damage to a player and offers may-draw")
    void abilityDamageToPlayerOffersMayDraw() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NivMizzetDracogenius());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // resolve the ping
        harness.passBothPriorities(); // resolve the may-draw trigger (ping ability blocks auto-pass)

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("{U}{R} deals 1 damage to a creature, destroying a 1-toughness creature")
    void abilityDestroys1ToughnessCreature() {
        addCreatureReady(player1, new NivMizzetDracogenius());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("{U}{R} does not kill a 2-toughness creature and does not offer may-draw")
    void abilityDoesNotKill2Toughness() {
        addCreatureReady(player1, new NivMizzetDracogenius());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ability does not require tapping and works while tapped")
    void canActivateWhileTapped() {
        Permanent niv = addCreatureReady(player1, new NivMizzetDracogenius());
        niv.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(niv.isTapped()).isTrue();
    }

    /**
     * Niv's ping ability is always "activatable" (no tap, mana-check skipped by auto-pass), so
     * combat-trigger auto-resolve stops with the may-draw still on the stack. Explicitly pass to
     * resolve it into the may choice.
     */
    private void resolveCombatAndMayDraw() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
