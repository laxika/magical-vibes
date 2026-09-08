package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LightningSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+0 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new LightningSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Lightning Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X=0 enters without +1/+0 counters")
    void entersWithNoCountersAtXZero() {
        harness.setHand(player1, List.of(new LightningSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Lightning Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        Permanent serpent = new Permanent(new LightningSerpent());
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Serpent");
        harness.assertInGraveyard(player1, "Lightning Serpent");
    }
}
