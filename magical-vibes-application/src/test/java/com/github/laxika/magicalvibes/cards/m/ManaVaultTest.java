package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElderDruid;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaVault.class, GrizzlyBears.class, ElderDruid.class})
class ManaVaultTest extends BaseCardTest {
    @Test
    @DisplayName("Tapping Mana Vault produces three colorless mana")
    void tappingProducesThreeColorlessMana() {
        addVault(player1, false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }
    @Test
    @DisplayName("Tapped Mana Vault does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent vault = addVault(player1, true);

        advanceToNextTurn(player2);

        assertThat(vault.isTapped()).isTrue();
    }
    @Test
    @DisplayName("Paying {4} during upkeep untaps Mana Vault")
    void payingFourUntapsVault() {
        Permanent vault = addVault(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vault.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
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
    @DisplayName("Mana Vault does not trigger during an opponent's draw step")
    void doesNotTriggerDuringOpponentsDrawStep() {
        addVault(player1, true);
        harness.setLife(player1, 20);

        advanceToDraw(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Untapping Mana Vault before its draw-step trigger resolves prevents the damage")
    void untappingBeforeDrawTriggerResolvesPreventsDamage() {
        addCreatureReady(player1, new ElderDruid());
        Permanent vault = addVault(player1, true);
        harness.setLife(player1, 20);

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, vault.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(vault.isTapped()).isFalse();
        harness.assertLife(player1, 20);
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
    private Permanent addVault(Player player, boolean tapped) {
        Permanent perm = new Permanent(new ManaVault());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip / empty-library loss
        harness.setLibrary(activePlayer, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
