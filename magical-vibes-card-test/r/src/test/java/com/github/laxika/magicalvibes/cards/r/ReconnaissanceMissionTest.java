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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("Each creature dealing combat damage produces an independent optional draw")
    void simultaneousAttackersProduceSeparateChoices() {
        addReconnaissanceMission();
        addReadyAttacker();
        addReadyAttacker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombatAndTrigger();
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature dealing combat damage does not trigger the Mission")
    void opposingCreatureDoesNotTrigger() {
        addReconnaissanceMission();
        addCreatureReady(player2, new GrizzlyBears()).setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not trigger a draw")
    void blockedCreatureDoesNotTrigger() {
        addReconnaissanceMission();
        addReadyAttacker();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A triggered draw still resolves after the Mission leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        addReconnaissanceMission();
        addReadyAttacker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof ReconnaissanceMission);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling pays the discard cost before drawing on resolution")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new ReconnaissanceMission()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Reconnaissance Mission");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling cannot be activated with less than two mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ReconnaissanceMission()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Reconnaissance Mission");
        harness.assertNotInGraveyard(player1, "Reconnaissance Mission");
        assertThat(gd.stack).isEmpty();
    }

    private void addReconnaissanceMission() {
        harness.addToBattlefield(player1, new ReconnaissanceMission());
    }

    private void addReadyAttacker() {
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
    }
}
