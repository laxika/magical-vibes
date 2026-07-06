package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

class RiggingRunnerTest extends BaseCardTest {

    // ===== Casting without raid =====

    @Test
    @DisplayName("Cast without raid — enters as 1/1 with no counters")
    void castWithoutRaid() {
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findRunner(player1);
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast without raid — creature enters battlefield")
    void creatureEntersWithoutRaid() {
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rigging Runner"));
    }

    // ===== Casting with raid =====

    @Test
    @DisplayName("Cast with raid — enters as 2/2 with one +1/+1 counter")
    void castWithRaid() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findRunner(player1);
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cast with raid — log message includes counter info")
    void castWithRaidLogMessage() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Rigging Runner") && log.contains("+1/+1 counter"));
    }

    // ===== No ETB trigger on the stack =====

    @Test
    @DisplayName("Raid counter is a replacement effect — no ETB trigger on stack")
    void raidCounterIsReplacementEffect() {
        markAttackedThisTurn();
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Stack should be empty after resolution — no triggered ability
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent attacking does not count for your raid")
    void opponentAttackDoesNotCountForRaid() {
        // Only opponent attacked — player1's raid should not be met
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.setHand(player1, List.of(new RiggingRunner()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent runner = findRunner(player1);
        assertThat(runner).isNotNull();
        assertThat(runner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private Permanent findRunner(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Rigging Runner"))
                .findFirst().orElse(null);
    }
}
