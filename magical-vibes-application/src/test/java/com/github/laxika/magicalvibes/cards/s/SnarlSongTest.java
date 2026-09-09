package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SnarlSongTest extends BaseCardTest {

    @Test
    @DisplayName("Converge creates two Fractals with X counters and gains X life")
    void createsFractalsAndGainsLifeForColorsSpent() {
        harness.setHand(player1, List.of(new SnarlSong()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
        harness.passBothPriorities();

        List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .toList();
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.getEffectivePower(gd, fractal)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, fractal)).isEqualTo(2);
        });
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Converge counts a repeated color only once")
    void repeatedColorCountsOnce() {
        harness.setHand(player1, List.of(new SnarlSong()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        harness.passBothPriorities();

        List<Permanent> fractals = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Fractal".equals(permanent.getCard().getName()))
                .toList();
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal ->
                assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
