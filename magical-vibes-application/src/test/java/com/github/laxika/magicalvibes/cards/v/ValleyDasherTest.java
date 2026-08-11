package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValleyDasherTest extends BaseCardTest {

    @Test
    @DisplayName("Valley Dasher must attack when able")
    void mustAttackWhenAble() {
        Permanent dasher = new Permanent(new ValleyDasher());
        dasher.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dasher);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Valley Dasher can attack immediately because it has haste")
    void canAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent dasher = new Permanent(new ValleyDasher());
        gd.playerBattlefields.get(player1.getId()).add(dasher);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
