package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExemplarOfLightTest extends BaseCardTest {

    private Permanent addExemplar() {
        return harness.addToBattlefieldAndReturn(player1, new ExemplarOfLight());
    }

    private void prepareLibrary(int cardCount) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(java.util.Collections.nCopies(cardCount, new Forest()));
    }

    private void castAngelAndResolveExemplarTriggers() {
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Angel of Mercy resolves.
        harness.passBothPriorities(); // Its life-gain effect resolves.
        harness.passBothPriorities(); // Exemplar of Light puts on a counter.
        resolveAllTriggers(); // Exemplar of Light's once-per-turn draw, if any, resolves.
    }

    @Test
    @DisplayName("Gaining life puts a counter on Exemplar of Light and draws a card")
    void gainingLifePutsCounterAndDraws() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        castAngelAndResolveExemplarTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("The counter trigger draws only once each turn")
    void counterTriggerDrawsOnlyOnceEachTurn() {
        Permanent exemplar = addExemplar();
        prepareLibrary(1);
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        castAngelAndResolveExemplarTriggers();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        castAngelAndResolveExemplarTriggers();

        assertThat(exemplar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
