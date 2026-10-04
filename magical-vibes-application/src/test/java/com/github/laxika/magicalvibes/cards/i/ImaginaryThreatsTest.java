package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EarthshakerKhenra;
import com.github.laxika.magicalvibes.cards.g.GraniticTitan;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImaginaryThreats.class, GraniticTitan.class, Manalith.class, EarthshakerKhenra.class})
class ImaginaryThreatsTest extends BaseCardTest {

    @Test
    @DisplayName("Forces the target opponent's creatures to attack and locks their next untap")
    void forcesAttackAndLocksNextUntap() {
        Permanent enemyBear = addCreatureReady(player2, new GraniticTitan());

        castImaginaryThreats(player2.getId());

        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        assertThat(enemyBear.isTapped()).isFalse();
        enemyBear.tap();
        advanceToNextTurn(player1);
        assertThat(enemyBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Leaves the caster's creatures and the target's non-creatures untouched")
    void doesNotAffectCasterCreaturesOrNonCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GraniticTitan());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new Manalith());

        castImaginaryThreats(player2.getId());

        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
        assertThat(ownBear.getSkipUntapCount()).isZero();
        assertThat(enemyArtifact.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Locked creatures stay tapped through the target's next untap step, then untap after")
    void lockedCreaturesDoNotUntapThenUntapLater() {
        Permanent enemyBear = addCreatureReady(player2, new GraniticTitan());

        castImaginaryThreats(player2.getId());
        enemyBear.tap(); // simulate the forced attack tapping it

        // Player2's next untap step: the lock keeps the creature tapped.
        advanceToNextTurn(player1);
        assertThat(enemyBear.isTapped()).isTrue();

        // The turn after (lock already consumed) it untaps normally.
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(enemyBear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The must-attack requirement forces an attack during the target opponent's combat")
    void mustAttackRequirementForcesAttackInCombat() {
        Permanent enemyBear = addCreatureReady(player2, new GraniticTitan());

        // Cast during the target opponent's turn, before their combat.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId());
        assertThat(harness.getCombatAttackService()
                .getMustAttackIndices(gd, player2.getId(), attackable)).contains(0);

        // Declaring no attackers is illegal — the creature must attack.
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself — the spell targets an opponent")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Creatures entering after resolution also miss the opponent's next untap")
    void laterArrivalsDoNotUntap() {
        castImaginaryThreats(player2.getId());
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new GraniticTitan());
        laterCreature.tap();

        advanceToNextTurn(player1);
        assertThat(laterCreature.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);
        assertThat(laterCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cycling pays two mana and discards immediately, then draws on resolution")
    void cyclingDiscardsThenDraws() {
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.setLibrary(player1, List.of(new Manalith()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Imaginary Threats");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Manalith");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Imaginary Threats");
        harness.assertNotInGraveyard(player1, "Imaginary Threats");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A hasty creature entering after resolution must also attack if able")
    void laterHastyCreatureMustAttack() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castImaginaryThreats(player2.getId());
        harness.addToBattlefield(player2, new EarthshakerKhenra());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).contains(0);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castImaginaryThreats(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
