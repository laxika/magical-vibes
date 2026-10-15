package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiltgroveStalker.class, GrizzlyBears.class, HillGiant.class})
class GiltgroveStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Giltgrove Stalker can't be blocked by a creature with power 2 or less")
    void cannotBeBlockedByCreatureWithPowerTwoOrLess() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blockerPerm.setSummoningSick(false);

        Permanent attackerPerm = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Giltgrove Stalker can be blocked by a creature with power greater than 2")
    void canBeBlockedByCreatureWithPowerGreaterThanTwo() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blockerPerm.setSummoningSick(false);

        Permanent attackerPerm = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        attackerPerm.setSummoningSick(false);
        attackerPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attackerPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-3, -2, -1, 0, 1})
    @DisplayName("Blocking restriction uses current power, including zero and negative power")
    void blockingRestrictionUsesModifiedPower(int powerModifier) {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setPowerModifier(powerModifier);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        List<BlockerAssignment> assignments = List.of(new BlockerAssignment(0, 0));
        if (powerModifier <= 0) {
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, assignments))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot block");
        } else {
            gs.declareBlockers(gd, player2, assignments);
            assertThat(blocker.isBlocking()).isTrue();
        }
    }

    @Test
    @DisplayName("A printed three-power creature reduced to two power cannot block")
    void weakenedThreePowerCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blocker.setPowerModifier(-1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiltgroveStalker());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }
}
