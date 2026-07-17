package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManaVaultTest extends BaseCardTest {

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping Mana Vault produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        addVault(player1, false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Mana Vault does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent vault = addVault(player1, true);

        advanceToNextTurn(player2);

        assertThat(vault.isTapped()).isTrue();
    }

    // ===== Upkeep: may pay {4} to untap =====

    @Test
    @DisplayName("Paying {4} during upkeep untaps Mana Vault")
    void payingFourUntapsVault() {
        Permanent vault = addVault(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vault.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Mana Vault tapped")
    void decliningLeavesVaultTapped() {
        Permanent vault = addVault(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vault.isTapped()).isTrue();
    }

    // ===== Draw step: if tapped, deals 1 damage to controller =====

    @Test
    @DisplayName("A tapped Mana Vault deals 1 damage to its controller at the draw step")
    void tappedVaultDealsOneDamageAtDrawStep() {
        addVault(player1, true);
        harness.setLife(player1, 20);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve draw-step trigger from stack

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("An untapped Mana Vault deals no damage at the draw step")
    void untappedVaultDealsNoDamage() {
        addVault(player1, false);
        harness.setLife(player1, 20);

        advanceToDraw(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    // ===== Helpers =====

    private Permanent addVault(Player player, boolean tapped) {
        Permanent perm = new Permanent(new ManaVault());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip / empty-library loss
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UPKEEP -> DRAW, fires draw-step trigger
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (untap)
    }
}
