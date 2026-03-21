package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwoHeadedGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        Permanent giant = new Permanent(new TwoHeadedGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Two-Headed Giant")))
                .isTrue();
    }

    @Test
    @DisplayName("Attack trigger flips two coins and logs the result")
    void attackFlipsTwoCoinsAndLogs() {
        Permanent giant = new Permanent(new TwoHeadedGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.gameLog)
                .anyMatch(log -> log.contains("flips two coins for Two-Headed Giant"));
    }

    @Test
    @DisplayName("Both heads grants double strike, both tails grants menace, mixed grants neither")
    void coinFlipOutcomesAreConsistent() {
        Permanent giant = new Permanent(new TwoHeadedGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        boolean hasDoubleStrike = giant.hasKeyword(Keyword.DOUBLE_STRIKE);
        boolean hasMenace = giant.hasKeyword(Keyword.MENACE);

        // Double strike and menace are mutually exclusive outcomes
        assertThat(hasDoubleStrike && hasMenace)
                .as("Cannot have both double strike and menace from the same flip")
                .isFalse();

        if (hasDoubleStrike) {
            assertThat(gd.gameLog).anyMatch(log -> log.contains("heads and heads"));
        } else if (hasMenace) {
            assertThat(gd.gameLog).anyMatch(log -> log.contains("tails and tails"));
        } else {
            // Mixed result — one heads, one tails
            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("heads and tails") || log.contains("tails and heads"));
        }
    }

    @Test
    @DisplayName("Granted keywords are lost at end of turn")
    void grantedKeywordsLostAtEndOfTurn() {
        Permanent giant = new Permanent(new TwoHeadedGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(giant);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        // Simulate end of turn cleanup
        giant.resetModifiers();

        assertThat(giant.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(giant.hasKeyword(Keyword.MENACE)).isFalse();
    }
}
