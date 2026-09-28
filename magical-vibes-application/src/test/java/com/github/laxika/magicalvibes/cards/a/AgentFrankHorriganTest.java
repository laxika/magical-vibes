package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentFrankHorrigan.class, GrizzlyBears.class})
class AgentFrankHorriganTest extends BaseCardTest {

    @Test
    @DisplayName("Agent Frank Horrigan has indestructible after attacking this turn")
    void hasIndestructibleAfterAttacking() {
        Permanent horrigan = addCreatureReady(player1, new AgentFrankHorrigan());

        assertThat(gqs.hasKeyword(gd, horrigan, Keyword.INDESTRUCTIBLE)).isFalse();

        declareAttackers(List.of(0));

        assertThat(gqs.hasKeyword(gd, horrigan, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Agent Frank Horrigan proliferates twice when it enters")
    void proliferatesTwiceWhenEntering() {
        Permanent bears = addCreatureWithCounter();
        castHorrigan();

        harness.passBothPriorities();
        harness.passBothPriorities();
        proliferateOn(bears);
        proliferateOn(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Agent Frank Horrigan proliferates twice when it attacks")
    void proliferatesTwiceWhenAttacking() {
        Permanent horrigan = addCreatureReady(player1, new AgentFrankHorrigan());
        Permanent bears = addCreatureWithCounter();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        proliferateOn(bears);
        proliferateOn(bears);

        assertThat(horrigan.isAttacking()).isTrue();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addCreatureWithCounter() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bears;
    }

    private void castHorrigan() {
        harness.setHand(player1, List.of(new AgentFrankHorrigan()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
    }

    private void proliferateOn(Permanent permanent) {
        harness.handleMultiplePermanentsChosen(player1, List.of(permanent.getId()));
    }
}
