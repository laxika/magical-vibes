package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GurmagSwiftwingTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = new Permanent(new GurmagSwiftwing());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike defeats a smaller blocker before regular combat damage")
    void firstStrikeDefeatsSmallerBlocker() {
        Permanent attacker = new Permanent(new GurmagSwiftwing());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        GurmagSwiftwing blockerCard = new GurmagSwiftwing();
        blockerCard.setPower(1);
        blockerCard.setToughness(1);
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gurmag Swiftwing");
        harness.assertInGraveyard(player2, "Gurmag Swiftwing");
    }

    @Test
    @DisplayName("Haste allows Gurmag Swiftwing to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new GurmagSwiftwing()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
