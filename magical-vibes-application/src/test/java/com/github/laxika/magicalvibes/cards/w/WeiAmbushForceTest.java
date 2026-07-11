package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeiAmbushForceTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new WeiAmbushForce());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Wei Ambush Force"));
    }

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void getsBoostWhenAttacking() {
        Permanent force = addCreatureReady(player1, new WeiAmbushForce());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.getPowerModifier()).isEqualTo(2);
        assertThat(force.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent force = addCreatureReady(player1, new WeiAmbushForce());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(force.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(force.getPowerModifier()).isEqualTo(0);
        assertThat(force.getToughnessModifier()).isEqualTo(0);
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
