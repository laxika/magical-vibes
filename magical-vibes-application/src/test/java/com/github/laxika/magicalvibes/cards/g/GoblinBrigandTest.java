package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinBrigandTest extends BaseCardTest {

    @Test
    @DisplayName("Declaring Goblin Brigand as attacker succeeds")
    void canDeclareAsAttacker() {
        harness.setLife(player2, 20);

        Permanent brigand = new Permanent(new GoblinBrigand());
        brigand.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(brigand);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declaring no attackers when Goblin Brigand can attack throws exception")
    void mustAttackWhenAble() {
        Permanent brigand = new Permanent(new GoblinBrigand());
        brigand.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(brigand);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Goblin Brigand does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        Permanent brigand = new Permanent(new GoblinBrigand());
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(brigand);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Cyclops has summoning sickness so empty declaration is valid
        gs.declareAttackers(gd, player1, List.of());

        assertThat(brigand.isAttacking()).isFalse();
    }
}
