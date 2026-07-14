package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FamiliarGroundTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control can't be blocked by two creatures while Familiar Ground is out")
    void creatureCannotBeBlockedByTwoCreatures() {
        Permanent familiarGround = new Permanent(new FamiliarGround());
        gd.playerBattlefields.get(player1.getId()).add(familiarGround);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blockerOne = new Permanent(new GrizzlyBears());
        blockerOne.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerOne);

        Permanent blockerTwo = new Permanent(new GrizzlyBears());
        blockerTwo.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerTwo);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("A single blocker is legal while Familiar Ground is out")
    void canBeBlockedByOneCreature() {
        Permanent familiarGround = new Permanent(new FamiliarGround());
        gd.playerBattlefields.get(player1.getId()).add(familiarGround);

        Permanent attacker = new Permanent(new GrizzlyBears());
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

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
