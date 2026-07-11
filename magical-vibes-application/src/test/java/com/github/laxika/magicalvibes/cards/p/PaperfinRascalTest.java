package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaperfinRascalTest extends BaseCardTest {

    private Permanent castPaperfinRascal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PaperfinRascal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (ETB clash trigger placed)
        harness.passBothPriorities(); // resolve ETB clash effect

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paperfin Rascal"))
                .findFirst().orElseThrow();
    }

    // ===== Won clash — put a +1/+1 counter on it =====

    @Test
    @DisplayName("Winning the clash puts a +1/+1 counter on Paperfin Rascal")
    void wonClashAddsCounter() {
        // Higher mana value on top for player1 (Grizzly Bears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(rascal.getEffectivePower()).isEqualTo(3);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Lost clash — no counter =====

    @Test
    @DisplayName("Losing the clash leaves Paperfin Rascal without a counter")
    void lostClashAddsNoCounter() {
        // Lower mana value on top for player1 (Forest MV 0 < Grizzly Bears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(rascal.getEffectivePower()).isEqualTo(2);
        assertThat(rascal.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Tie — a clash is only won on a strictly greater mana value (CR 701.29c) =====

    @Test
    @DisplayName("An equal mana value tie is not a win, so no counter is added")
    void tiedClashAddsNoCounter() {
        // Equal mana values (both Grizzly Bears MV 2) → no one wins the clash.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        Permanent rascal = castPaperfinRascal();

        assertThat(rascal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }
}
