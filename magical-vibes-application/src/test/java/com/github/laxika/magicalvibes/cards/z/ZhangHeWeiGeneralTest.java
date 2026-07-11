package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZhangHeWeiGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Zhang He, Wei General"));
    }

    @Test
    @DisplayName("Each other creature you control gets +1/+0 when Zhang He attacks")
    void otherCreaturesGetBoost() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Zhang He does not boost itself")
    void doesNotBoostItself() {
        Permanent zhangHe = addCreatureReady(player1, new ZhangHeWeiGeneral());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(zhangHe.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enemy.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ZhangHeWeiGeneral());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void resolveAllTriggers() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
