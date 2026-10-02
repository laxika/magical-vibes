package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

        resolveAllTriggers();
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

    @Test
    @DisplayName("Indestructible persists after combat and expires on the next turn")
    void indestructibleLastsOnlyForTheTurnItAttacked() {
        Permanent horrigan = addCreatureReady(player1, new AgentFrankHorrigan());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(horrigan.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, horrigan, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, horrigan, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Entering finishes after two independent proliferate choices")
    void enteringFinishesAfterTwoIndependentChoices() {
        Permanent opponent = addCreatureReady(player2, new AgentFrankHorrigan());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castHorrigan();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        proliferateOn(opponent);

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking proliferates every existing player counter kind exactly twice")
    void attackingProliferatesPlayerCountersExactlyTwice() {
        addCreatureReady(player1, new AgentFrankHorrigan());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 2);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("Entering with no counters available completes without a choice")
    void enteringWithNoCountersCompletes() {
        castHorrigan();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Agent Frank Horrigan");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreatureWithCounter() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bears;
    }

    private void castHorrigan() {
        harness.castFromHand(player1, new AgentFrankHorrigan(), "{5}{B}{G}");
    }

    private void proliferateOn(Permanent permanent) {
        harness.handleMultiplePermanentsChosen(player1, List.of(permanent.getId()));
    }
}
