package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed(Degavolver.class)
class DegavolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Degavolver enters as a 1/1 without granted abilities")
    void withoutKickers() {
        Permanent degavolver = castDegavolver();

        assertThat(degavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, degavolver, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(degavolver.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Black kicker adds two counters and the regeneration ability")
    void blackKicker() {
        addMana(ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Degavolver()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent degavolver = findDegavolver();
        assertThat(degavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, degavolver, Keyword.FIRST_STRIKE)).isFalse();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(degavolver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Red kicker adds one counter and first strike")
    void redKicker() {
        addMana(ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Degavolver()));

        castWithRedKicker();
        harness.passBothPriorities();

        Permanent degavolver = findDegavolver();
        assertThat(degavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, degavolver, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Both kickers apply both sets of effects")
    void bothKickers() {
        addMana(ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Degavolver()));

        castWithBothKickers();
        harness.passBothPriorities();

        Permanent degavolver = findDegavolver();
        assertThat(degavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, degavolver, Keyword.FIRST_STRIKE)).isTrue();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(degavolver.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent castDegavolver() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new Degavolver()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findDegavolver();
    }

    private void castWithRedKicker() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{R}"), false);
    }

    private void castWithBothKickers() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{R}"), false);
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }

    private Permanent findDegavolver() {
        return findPermanent(player1, "Degavolver");
    }
}
