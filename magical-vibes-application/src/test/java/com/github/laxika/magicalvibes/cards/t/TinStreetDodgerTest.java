package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TinStreetDodgerTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prevents non-defender creatures from blocking this turn")
    void nonDefenderCannotBlock() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        activateDodger(dodger);
        dodger.setAttacking(true);
        beginDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability allows creatures with defender to block this turn")
    void defenderCanBlock() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new WallOfWood());
        activateDodger(dodger);
        dodger.setAttacking(true);
        beginDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent dodger = addReadyDodger(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        activateDodger(dodger);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        dodger.setAttacking(true);
        beginDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(dodger);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyDodger(Player player) {
        return addCreatureReady(player, new TinStreetDodger());
    }

    private void activateDodger(Permanent dodger) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(dodger), null, null);
        harness.passBothPriorities();
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
