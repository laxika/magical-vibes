package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.u.UnholyStrength;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tetravus.class})
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

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "2"); // remove 2 counters -> create 2 Tetravite tokens

        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, tetravus)).isEqualTo(2);

        List<Permanent> tokens = tetraviteTokens();
        assertThat(tokens).hasSize(2);
        Permanent token = tokens.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, token)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.cantBeEnchantedByOtherAuras(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Upkeep: choosing to remove zero counters creates no tokens")
    void removeZeroCreatesNothing() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
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
        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "3");

        List<Permanent> tokens = tetraviteTokens();
        assertThat(tokens).hasSize(3);
        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        List<java.util.UUID> tokenIds = tokens.stream().map(Permanent::getId).toList();

        // Second upkeep: exile those tokens to shuttle the counters back onto Tetravus.
        advanceToUpkeep(player1);
        resolveTriggersUntilInput(); // exile trigger resolves first (tokens exist) -> multi-permanent choice
        harness.handleMultiplePermanentsChosen(player1, tokenIds);
        resolveTriggersUntilInput(); // remove trigger resolves next -> counters present -> number choice
        harness.handleListChoice(player1, "0"); // decline making new tokens

        assertThat(tetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(tetraviteTokens()).isEmpty();
    }

    @Test
    @CardUsed(UnholyStrength.class)
    @DisplayName("Tetravite tokens can't be enchanted by an Aura")
    void tetraviteTokensCannotBeEnchanted() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "1");

        Permanent token = tetraviteTokens().getFirst();
        harness.setHand(player1, List.of(new UnholyStrength()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    @Test
    @DisplayName("Each Tetravus can exile only the Tetravite tokens it created")
    void onlyTheCreatingTetravusCanExileItsTokens() {
        Permanent firstTetravus = addCreatureReady(player1, new Tetravus());
        firstTetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondTetravus = addCreatureReady(player1, new Tetravus());

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "1");

        Permanent token = tetraviteTokens().getFirst();

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleMultiplePermanentsChosen(player1, List.of(token.getId()));
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "0");

        assertThat(firstTetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTetravus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tetraviteTokens()).isEmpty();
    }

    @Test
    @CardUsed(Disenchant.class)
    @DisplayName("The exile trigger still works if Tetravus leaves before resolution")
    void exileTriggerWorksAfterSourceLeaves() {
        Permanent tetravus = addCreatureReady(player1, new Tetravus());
        tetravus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        resolveTriggersUntilInput();
        harness.handleListChoice(player1, "1");

        Permanent token = tetraviteTokens().getFirst();
        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, tetravus.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tetravus);

        resolveTriggersUntilInput();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player1, List.of(token.getId()));
        assertThat(tetraviteTokens()).isEmpty();
    }

    private void resolveTriggersUntilInput() {
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }

    private List<Permanent> tetraviteTokens() {
        return findPermanents(player1, "Tetravite");
    }
}
