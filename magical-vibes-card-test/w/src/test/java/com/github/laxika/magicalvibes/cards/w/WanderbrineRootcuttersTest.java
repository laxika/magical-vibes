package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScuzzbackScrapper;
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

@CardUsed({WanderbrineRootcutters.class, GrizzlyBears.class, FugitiveWizard.class, ScuzzbackScrapper.class})
class WanderbrineRootcuttersTest extends BaseCardTest {

    @Test
    @DisplayName("Wanderbrine Rootcutters can't be blocked by a green creature")
    void cannotBeBlockedByGreenCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new WanderbrineRootcutters());
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
    @DisplayName("Wanderbrine Rootcutters can be blocked by a non-green creature")
    void canBeBlockedByNonGreenCreature() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        blockerPerm.setSummoningSick(false);

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new WanderbrineRootcutters());
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
    @DisplayName("A red and green creature cannot block Wanderbrine Rootcutters")
    void cannotBeBlockedByMulticoloredGreenCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ScuzzbackScrapper());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WanderbrineRootcutters());
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

    @Test
    @DisplayName("A blue and black creature can block Wanderbrine Rootcutters")
    void canBeBlockedByMulticoloredNonGreenCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WanderbrineRootcutters());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WanderbrineRootcutters());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
