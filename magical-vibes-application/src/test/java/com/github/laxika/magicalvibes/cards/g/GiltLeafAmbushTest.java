package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiltLeafAmbushTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GiltLeafAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // {2}{G}
    }

    // Caster (player1) wins the clash: their revealed top card (MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster (player1) loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest(), new Forest()));
    }

    private List<Permanent> elfWarriors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .toList();
    }

    @Test
    @DisplayName("Always creates two 1/1 Elf Warrior tokens")
    void createsTwoTokens() {
        prepare();
        stackClashLossForCaster();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> {
            assertThat(t.getEffectivePower()).isEqualTo(1);
            assertThat(t.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Winning the clash grants deathtouch to both created tokens")
    void wonClashGrantsDeathtouch() {
        prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isTrue());
    }

    @Test
    @DisplayName("Losing the clash leaves the tokens without deathtouch")
    void lostClashNoDeathtouch() {
        prepare();
        stackClashLossForCaster();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("Clash-win deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        prepare();
        stackClashWinForCaster();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> tokens = elfWarriors();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(t -> assertThat(t.hasKeyword(Keyword.DEATHTOUCH)).isFalse());
    }
}
