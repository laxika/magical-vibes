package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GravelgillDuo;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.s.SickleRipper;
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

@CardUsed({RavensRunDragoon.class, SickleRipper.class, SafeholdElite.class, GravelgillDuo.class})
class RavensRunDragoonTest extends BaseCardTest {

    @Test
    @DisplayName("Raven's Run Dragoon can't be blocked by a black creature")
    void cannotBeBlockedByBlackCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new SickleRipper());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RavensRunDragoon());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raven's Run Dragoon can be blocked by a non-black creature")
    void canBeBlockedByNonBlackCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new SafeholdElite());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RavensRunDragoon());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Raven's Run Dragoon can't be blocked by a creature that is both black and blue")
    void cannotBeBlockedByMulticoloredBlackCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GravelgillDuo());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RavensRunDragoon());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }
}
