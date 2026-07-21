package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("wins the coin flip"));
        } else {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the coin flip"));
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
