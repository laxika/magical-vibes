package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TattermungeManiacTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring Tattermunge Maniac as attacker succeeds and deals 2 damage")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        Permanent maniac = new Permanent(new TattermungeManiac());
        maniac.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(maniac);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers when Tattermunge Maniac can attack throws exception")
    void mustAttackWhenAble() {
        Permanent maniac = new Permanent(new TattermungeManiac());
        maniac.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(maniac);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Tattermunge Maniac while declaring another creature throws exception")
    void mustBeIncludedAmongAttackers() {
        Permanent maniac = new Permanent(new TattermungeManiac());
        maniac.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(maniac);

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
    @DisplayName("Tattermunge Maniac with summoning sickness is not forced to attack")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent maniac = new Permanent(new TattermungeManiac());
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(maniac);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
