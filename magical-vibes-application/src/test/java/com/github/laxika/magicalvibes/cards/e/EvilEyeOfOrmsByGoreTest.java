package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvilEyeOfOrmsByGoreTest extends BaseCardTest {

    // ===== Non-Eye creatures you control can't attack =====

    @Test
    @DisplayName("A non-Eye creature you control cannot attack while Evil Eye is on the battlefield")
    void nonEyeCreatureCannotAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evil Eye itself (an Eye) can still attack")
    void evilEyeCanAttack() {
        Permanent evilEye = new Permanent(new EvilEyeOfOrmsByGore());
        evilEye.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(evilEye);

        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Evil Eye is 3/6, unblocked
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An Eye creature you control can attack")
    void eyeCreatureCanAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());
        harness.setLife(player2, 20);

        Card eye = new Card();
        eye.setName("Test Eye");
        eye.setType(CardType.CREATURE);
        eye.setSubtypes(List.of(CardSubtype.EYE));
        eye.setPower(2);
        eye.setToughness(2);
        Permanent eyePerm = new Permanent(eye);
        eyePerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(eyePerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int eyeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(eyePerm);
        gs.declareAttackers(gd, player1, List.of(eyeIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The opponent's non-Eye creatures are unaffected (restriction is controller-scoped)")
    void opponentNonEyeCreatureCanAttack() {
        harness.addToBattlefield(player1, new EvilEyeOfOrmsByGore());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        gs.declareAttackers(gd, player2, List.of(bearsIndex));

        assertThat(bears.isAttacking()).isTrue();
    }

    // ===== Can't be blocked except by Walls =====

    @Test
    @DisplayName("Evil Eye cannot be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        Permanent evilEye = new Permanent(new EvilEyeOfOrmsByGore());
        evilEye.setSummoningSick(false);
        evilEye.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(evilEye);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Walls");
    }

    @Test
    @DisplayName("Evil Eye can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent evilEye = new Permanent(new EvilEyeOfOrmsByGore());
        evilEye.setSummoningSick(false);
        evilEye.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(evilEye);

        Permanent wall = new Permanent(new AngelicWall());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
