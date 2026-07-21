package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImaginaryThreatsTest extends BaseCardTest {

    @Test
    @DisplayName("Forces the target opponent's creatures to attack and locks their next untap")
    void forcesAttackAndLocksNextUntap() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        castImaginaryThreats(player2.getId());

        assertThat(enemyBear.isMustAttackThisTurn()).isTrue();
        assertThat(enemyBear.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaves the caster's creatures and the target's non-creatures untouched")
    void doesNotAffectCasterCreaturesOrNonCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new JayemdaeTome());
        Permanent enemyArtifact = gd.playerBattlefields.get(player2.getId()).getFirst();

        castImaginaryThreats(player2.getId());

        assertThat(ownBear.isMustAttackThisTurn()).isFalse();
        assertThat(ownBear.getSkipUntapCount()).isZero();
        assertThat(enemyArtifact.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Locked creatures stay tapped through the target's next untap step, then untap after")
    void lockedCreaturesDoNotUntapThenUntapLater() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

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
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());

        // Cast during the target opponent's turn, before their combat.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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

    private void castImaginaryThreats(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ImaginaryThreats()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
