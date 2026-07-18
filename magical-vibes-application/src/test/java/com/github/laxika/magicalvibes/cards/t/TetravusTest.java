package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TetravusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters, making it a 4/4")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new Tetravus()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent tetravus = findPermanent(player1, "Tetravus");
        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, tetravus)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tetravus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep: remove +1/+1 counters to create that many flying Tetravite tokens")
    void removeCountersCreatesTokens() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        beginUpkeep();
        resolveStack();
        harness.handleListChoice(player1, "2"); // remove 2 counters -> create 2 Tetravite tokens

        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tetravus)).isEqualTo(2);

        List<Permanent> tokens = tetraviteTokens();
        assertThat(tokens).hasSize(2);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.cantBeEnchantedByOtherAuras(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Upkeep: choosing to remove zero counters creates no tokens")
    void removeZeroCreatesNothing() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        beginUpkeep();
        resolveStack();
        harness.handleListChoice(player1, "0"); // decline

        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(tetraviteTokens()).isEmpty();
    }

    @Test
    @DisplayName("Upkeep: exile tokens created with Tetravus to put that many +1/+1 counters back on it")
    void exileTokensAddsCounters() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        // First upkeep: turn all three counters into three Tetravite tokens.
        beginUpkeep();
        resolveStack();
        harness.handleListChoice(player1, "3");

        List<Permanent> tokens = tetraviteTokens();
        assertThat(tokens).hasSize(3);
        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        List<java.util.UUID> tokenIds = tokens.stream().map(Permanent::getId).toList();

        // Second upkeep: exile those tokens to shuttle the counters back onto Tetravus.
        beginUpkeep();
        resolveStack(); // exile trigger resolves first (tokens exist) -> multi-permanent choice
        harness.handleMultiplePermanentsChosen(player1, tokenIds);
        resolveStack(); // remove trigger resolves next -> counters present -> number choice
        harness.handleListChoice(player1, "0"); // decline making new tokens

        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(tetraviteTokens()).isEmpty();
    }

    private void beginUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // UNTAP -> UPKEEP: both upkeep triggers go on the stack
    }

    /** Pass priority a few times to resolve stacked triggers; no-ops once an interaction is pending. */
    private void resolveStack() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> tetraviteTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tetravite"))
                .toList();
    }
}
