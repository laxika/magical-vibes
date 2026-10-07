package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentedSoul.class, RuneclawBear.class, TurnToFrog.class})
class TormentedSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Tormented Soul cannot be blocked")
    void cannotBeBlocked() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new TormentedSoul());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Tormented Soul cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent soul = harness.addToBattlefieldAndReturn(player2, new TormentedSoul());
        soul.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Unblocked Tormented Soul deals 1 damage to the defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new TormentedSoul());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tormented Soul can be blocked after losing its abilities")
    void canBeBlockedAfterLosingAbilities() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new TormentedSoul());
        soul.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        turnToFrog(soul);
        soul.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.getBlockingTargets()).containsExactly(0);
    }

    @Test
    @DisplayName("Tormented Soul can block after losing its abilities, even while summoning sick")
    void canBlockAfterLosingAbilities() {
        Permanent soul = harness.addToBattlefieldAndReturn(player2, new TormentedSoul());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);

        turnToFrog(soul);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(soul.getBlockingTargets()).containsExactly(0);
    }

    private void turnToFrog(Permanent soul) {
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, soul.getId());
        harness.passBothPriorities();
    }
}
