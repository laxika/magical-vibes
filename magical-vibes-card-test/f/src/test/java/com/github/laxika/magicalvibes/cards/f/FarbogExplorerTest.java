package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({FarbogExplorer.class, Swamp.class})
class FarbogExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Farbog Explorer cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new Swamp());

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new FarbogExplorer());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Farbog Explorer can be blocked when defending player does not control a Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new FarbogExplorer());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
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
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void attackersSwampDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new FarbogExplorer());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped defending Swamp still prevents blocking")
    void tappedDefendingSwampPreventsBlocking() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        swamp.tap();
        harness.addToBattlefield(player2, new FarbogExplorer());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
