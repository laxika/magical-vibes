package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed(Cetavolver.class)
class CetavolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Cetavolver enters without counters or granted keywords")
    void withoutKickers() {
        Permanent cetavolver = castCetavolver();

        assertThat(cetavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Red kicker adds two counters and first strike")
    void redKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Cetavolver()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of(), false);
        harness.passBothPriorities();

        Permanent cetavolver = findCetavolver();
        assertThat(cetavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Green kicker adds one counter and trample")
    void greenKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Cetavolver()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{G}"), false);
        harness.passBothPriorities();

        Permanent cetavolver = findCetavolver();
        assertThat(cetavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Both kickers add three counters and both keywords")
    void bothKickers() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Cetavolver()));

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{G}"), false);
        harness.passBothPriorities();

        Permanent cetavolver = findCetavolver();
        assertThat(cetavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, cetavolver, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castCetavolver() {
        addBaseMana();
        harness.setHand(player1, List.of(new Cetavolver()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findCetavolver();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent findCetavolver() {
        return findPermanent(player1, "Cetavolver");
    }
}
