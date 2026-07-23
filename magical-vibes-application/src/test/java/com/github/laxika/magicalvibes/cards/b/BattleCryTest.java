package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattleCryTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps white creatures you control, not nonwhite or opponent's")
    void untapsOnlyControlledWhiteCreatures() {
        Permanent white = new Permanent(new SavannahLions());
        white.tap();
        white.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(white);

        Permanent green = new Permanent(new GrizzlyBears());
        green.tap();
        green.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(green);

        Permanent oppWhite = new Permanent(new SavannahLions());
        oppWhite.tap();
        oppWhite.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppWhite);

        harness.setHand(player1, List.of(new BattleCry()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(white.isTapped()).isFalse();
        assertThat(green.isTapped()).isTrue();
        assertThat(oppWhite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Whenever a creature blocks this turn, it gets +0/+1 until end of turn")
    void blockerGetsPlusZeroPlusOne() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setHand(player2, List.of(new BattleCry()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        // Delayed trigger on the stack — resolve it.
        harness.passBothPriorities();
        resolveStack();

        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
        assertThat(blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Blocker boost wears off at end of turn")
    void blockerBoostExpiresAtEndOfTurn() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.setHand(player2, List.of(new BattleCry()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();
        resolveStack();

        assertThat(blocker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getToughnessModifier()).isZero();
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
