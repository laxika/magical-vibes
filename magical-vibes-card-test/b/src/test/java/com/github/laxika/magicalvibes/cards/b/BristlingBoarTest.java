package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
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

@CardUsed({BristlingBoar.class, GreenwoodSentinel.class})
class BristlingBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Bristling Boar can be blocked by one creature")
    void canBeBlockedByOneCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Bristling Boar cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Bristling Boar can remain unblocked and deal combat damage")
    void canRemainUnblocked() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Each Bristling Boar may be blocked by a separate creature")
    void blockerLimitAppliesToEachAttackerSeparately() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        first.setSummoningSick(false);
        first.setAttacking(true);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        second.setSummoningSick(false);
        second.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        assertThat(firstBlocker.getBlockingTargets()).containsExactly(0);
        assertThat(secondBlocker.getBlockingTargets()).containsExactly(1);
    }

    @Test
    @DisplayName("Bristling Boar does not restrict blockers of other attackers")
    void doesNotRestrictOtherAttackers() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        boar.setSummoningSick(false);
        boar.setAttacking(true);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        other.setSummoningSick(false);
        other.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1)));

        assertThat(firstBlocker.getBlockingTargets()).containsExactly(1);
        assertThat(secondBlocker.getBlockingTargets()).containsExactly(1);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    @DisplayName("Losing all abilities removes Bristling Boar's blocker limit")
    void losingAbilitiesRemovesBlockerLimit() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.getBlockingTargets()).containsExactly(0);
        assertThat(secondBlocker.getBlockingTargets()).containsExactly(0);
    }
}
