package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SunSentinel;
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

@CardUsed({RiverDarter.class, RaptorCompanion.class, SunSentinel.class})
class RiverDarterTest extends BaseCardTest {

    @Test
    @DisplayName("River Darter can't be blocked by a Dinosaur")
    void cannotBeBlockedByDinosaur() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        blocker.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("River Darter can be blocked by a non-Dinosaur creature")
    void canBeBlockedByNonDinosaurCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SunSentinel());
        blocker.setSummoningSick(false);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("River Darter does not prevent Dinosaurs from blocking other attackers")
    void dinosaurCanBlockAnotherAttacker() {
        Permanent riverDarter = harness.addToBattlefieldAndReturn(player1, new RiverDarter());
        riverDarter.setSummoningSick(false);
        riverDarter.setAttacking(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new SunSentinel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargets()).containsExactly(attackerIndex);
    }
}
