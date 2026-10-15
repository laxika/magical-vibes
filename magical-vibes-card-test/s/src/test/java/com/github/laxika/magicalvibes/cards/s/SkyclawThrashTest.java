package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclawThrash.class})
class SkyclawThrashTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the coin-flip trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new SkyclawThrash());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Skyclaw Thrash"));
    }

    @Test
    @DisplayName("Coin flip either grants +1/+1 and flying (win) or nothing (loss)")
    void coinFlipAppliesExactlyOneBranch() {
        Permanent thrash = addCreatureReady(player1, new SkyclawThrash());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        boolean won = thrash.getPowerModifier() == 1
                && thrash.getToughnessModifier() == 1
                && gqs.hasKeyword(gd, thrash, Keyword.FLYING);
        boolean lost = thrash.getPowerModifier() == 0
                && thrash.getToughnessModifier() == 0
                && !gqs.hasKeyword(gd, thrash, Keyword.FLYING);

        assertThat(won != lost)
                .as("must get exactly the +1/+1-and-flying win branch or the no-op loss branch")
                .isTrue();

        if (won) {
            assertThat(gameLogContains("wins the coin flip")).isTrue();
        } else {
            assertThat(gameLogContains("loses the coin flip")).isTrue();
        }
    }

    @Test
    @DisplayName("The win-branch boost and flying wear off at end of turn")
    void buffWearsOffAtEndOfTurn() {
        Permanent thrash = addCreatureReady(player1, new SkyclawThrash());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        // Whatever the flip produced, nothing may persist past cleanup (only the win branch
        // grants anything, and that grant is until-end-of-turn).
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(thrash.getPowerModifier()).isZero();
        assertThat(thrash.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, thrash, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Only the attacking Thrash triggers and receives the coin-flip reward")
    void nonattackingCopyDoesNotTriggerOrReceiveReward() {
        Permanent attacker = addCreatureReady(player1, new SkyclawThrash());
        Permanent nonattacker = addCreatureReady(player1, new SkyclawThrash());
        Permanent opponent = addCreatureReady(player2, new SkyclawThrash());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gameLogContains("wins the coin flip")).isFalse();
        assertThat(gameLogContains("loses the coin flip")).isFalse();
        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isFalse();

        resolveAllTriggers();

        boolean won = gameLogContains("wins the coin flip");
        assertThat(attacker.getPowerModifier()).isEqualTo(won ? 1 : 0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(won ? 1 : 0);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isEqualTo(won);
        for (Permanent other : List.of(nonattacker, opponent)) {
            assertThat(other.getPowerModifier()).isZero();
            assertThat(other.getToughnessModifier()).isZero();
            assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        }
    }

    @Test
    @DisplayName("Removing the attacker does not prevent its pending coin flip")
    void pendingTriggerStillFlipsAfterSourceLeaves() {
        Permanent attacker = addCreatureReady(player1, new SkyclawThrash());
        Permanent other = addCreatureReady(player1, new SkyclawThrash());

        declareAttackers(player1, List.of(0));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("wins the coin flip")
                ^ gameLogContains("loses the coin flip")).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }
}
