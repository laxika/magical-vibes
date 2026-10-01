package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaperfinRascal.class, Forest.class})
class PaperfinRascalTest extends BaseCardTest {

    private Permanent castPaperfinRascal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PaperfinRascal(), "{2}{U}");
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        return findPermanent(player1, "Paperfin Rascal");
    }

    // ===== Won clash — put a +1/+1 counter on it =====

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Paperfin Rascal")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Paperfin Rascal MV 3 > Forest MV 0) → player1 wins.
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new Forest()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rascal.getEffectivePower()).isEqualTo(3);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Lost clash — no counter =====

    @Test
    @DisplayName("Losing the clash leaves Paperfin Rascal without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Forest MV 0 < Paperfin Rascal MV 3) → player1 loses.
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rascal.getEffectivePower()).isEqualTo(2);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Tie — a clash is only won on a strictly greater mana value (CR 701.30d) =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        // Equal mana values (both Paperfin Rascals MV 3) → no one wins the clash.
        harness.setLibrary(player1, List.of(new PaperfinRascal()));
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("An empty library cannot win the clash, so no counter is added")
    void emptyLibraryAddsNoCounter() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new PaperfinRascal()));

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rascal.getEffectivePower()).isEqualTo(2);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(2);
    }
}
