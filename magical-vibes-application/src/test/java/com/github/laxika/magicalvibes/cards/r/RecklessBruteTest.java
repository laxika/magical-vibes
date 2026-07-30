package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring no attackers while Reckless Brute can attack is rejected")
    void mustAttackWhenAble() {
        Permanent brute = new Permanent(new RecklessBrute());
        brute.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(brute);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Reckless Brute while declaring another attacker is rejected")
    void mustBeIncludedAmongAttackers() {
        Permanent brute = new Permanent(new RecklessBrute());
        brute.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(brute);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Reckless Brute attacks for 3 when declared")
    void attacksForThree() {
        harness.setLife(player2, 20);

        Permanent brute = new Permanent(new RecklessBrute());
        brute.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(brute);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Haste lets a summoning-sick Reckless Brute attack, so it is still forced to")
    void hasteMakesItAttackTheTurnItEnters() {
        Permanent brute = new Permanent(new RecklessBrute());
        gd.playerBattlefields.get(player1.getId()).add(brute);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped Reckless Brute imposes no attack requirement")
    void tappedBruteIsNotForcedToAttack() {
        Permanent brute = new Permanent(new RecklessBrute());
        brute.setSummoningSick(false);
        brute.tap();
        gd.playerBattlefields.get(player1.getId()).add(brute);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(brute.isAttacking()).isFalse();
    }
}
