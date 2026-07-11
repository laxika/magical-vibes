package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LurkingNightstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(2);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("+2/+0 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nightstalker.getPowerModifier()).isEqualTo(0);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost while it sits on the battlefield without attacking")
    void noBoostWithoutAttacking() {
        Permanent nightstalker = addCreatureReady(player1, new LurkingNightstalker());

        assertThat(nightstalker.getPowerModifier()).isEqualTo(0);
        assertThat(nightstalker.getToughnessModifier()).isEqualTo(0);
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
