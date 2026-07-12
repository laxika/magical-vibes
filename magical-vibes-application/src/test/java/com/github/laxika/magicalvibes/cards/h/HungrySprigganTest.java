package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HungrySprigganTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 until end of turn when it attacks")
    void boostsOnAttack() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spriggan.getPowerModifier()).isEqualTo(0);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(0);
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
