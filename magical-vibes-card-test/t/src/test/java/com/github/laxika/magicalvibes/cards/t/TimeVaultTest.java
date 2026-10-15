package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeVault.class})
class TimeVaultTest extends BaseCardTest {

    private Permanent putVaultOnBattlefield() {
        return harness.enterBattlefieldAndReturn(player1, new TimeVault());
    }

    private void advanceToPlayer1TurnStart() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        Permanent vault = putVaultOnBattlefield();

        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May skip the controller's turn and untap")
    void maySkipTurnAndUntap() {
        Permanent vault = putVaultOnBattlefield();
        advanceToPlayer1TurnStart();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vault.isTapped()).isFalse();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining leaves it tapped through the untap step")
    void decliningLeavesItTapped() {
        Permanent vault = putVaultOnBattlefield();
        advanceToPlayer1TurnStart();

        harness.handleMayAbilityChosen(player1, false);
        harness.performUntapStep(player1);

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Tap ability queues an extra turn")
    void tapAbilityQueuesExtraTurn() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new TimeVault());
        vault.untap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Declining both tapped Vaults starts the turn without repeating either choice")
    void decliningAllVaultsStartsTurn() {
        Permanent first = putVaultOnBattlefield();
        Permanent second = putVaultOnBattlefield();
        advanceToPlayer1TurnStart();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Skipping one turn untaps only the chosen Vault")
    void skippingTurnUntapsOnlyOneVault() {
        Permanent first = putVaultOnBattlefield();
        Permanent second = putVaultOnBattlefield();
        advanceToPlayer1TurnStart();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(first.isTapped() && second.isTapped()).isFalse();
        assertThat(first.isTapped() || second.isTapped()).isTrue();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An untapped Vault does not offer to skip the turn")
    void untappedVaultDoesNotOfferChoice() {
        Permanent vault = putVaultOnBattlefield();
        vault.untap();

        advanceToPlayer1TurnStart();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(vault.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Vault does not offer to skip an opponent's turn")
    void doesNotReplaceOpponentsTurn() {
        Permanent vault = putVaultOnBattlefield();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(vault.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An extra turn can be skipped without skipping the following normal turn")
    void canSkipExtraTurnWithoutSkippingNormalTurn() {
        Permanent vault = putVaultOnBattlefield();
        vault.untap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        advanceToPlayer1TurnStart();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(vault.isTapped()).isFalse();
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentTurnIsExtraTurn).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
