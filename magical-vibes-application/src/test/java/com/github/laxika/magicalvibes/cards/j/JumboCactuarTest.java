package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JumboCactuar.class)
class JumboCactuarTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gives Jumbo Cactuar +9999/+0 until end of turn")
    void attackingBoostsPower() {
        Permanent cactuar = addCreatureReady(player1, new JumboCactuar());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(cactuar.getPowerModifier()).isEqualTo(9999);
        assertThat(cactuar.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, cactuar)).isEqualTo(10000);
        assertThat(gqs.getEffectiveToughness(gd, cactuar)).isEqualTo(7);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        Permanent cactuar = addCreatureReady(player1, new JumboCactuar());
        harness.setLife(player2, 20_000);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        cactuar.setAttacking(false);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(cactuar.getPowerModifier()).isZero();
        assertThat(cactuar.getToughnessModifier()).isZero();
        assertThat(gqs.getEffectivePower(gd, cactuar)).isEqualTo(1);
    }
}
