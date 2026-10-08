package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TippyToeTerrificPartner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnarlSong.class, TippyToeTerrificPartner.class})
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

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCard().isToken()).isTrue();
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

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCard().isToken()).isTrue();
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        });
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Converge counts all five colors spent and retains the cast-time count")
    void fiveColorsRemainFixedAfterManaPoolChanges() {
        harness.setHand(player1, List.of(new SnarlSong()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.passBothPriorities();

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(2);
        assertThat(fractals).allSatisfy(fractal -> {
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
            assertThat(gqs.getEffectivePower(gd, fractal)).isEqualTo(5);
            assertThat(gqs.getEffectiveToughness(gd, fractal)).isEqualTo(5);
        });
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Snarl Song");
    }

    @Test
    @DisplayName("Creating the two Fractals is one token-creation event")
    void foodReplacementAppliesOnceToBothFractals() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        harness.setHand(player1, List.of(new SnarlSong()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Fractal")).hasSize(2);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
        harness.assertLife(player1, 21);
    }
}
