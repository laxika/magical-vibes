package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(Anavolver.class)
class AnavolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Anavolver enters without counters, flying, or regeneration")
    void withoutKickers() {
        Permanent anavolver = castAnavolver();

        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isFalse();
        assertThat(anavolver.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Blue kicker adds two counters and flying")
    void blueKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBlueKicker();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isTrue();
        assertThat(anavolver.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Black kicker adds one counter and the regeneration ability")
    void blackKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBlackKicker();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isFalse();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(anavolver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both kickers add three counters, flying, and regeneration")
    void bothKickers() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBothKickers();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isTrue();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(anavolver.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent castAnavolver() {
        addBaseMana();
        harness.setHand(player1, List.of(new Anavolver()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findAnavolver();
    }

    private void castWithBlueKicker() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of(), false);
    }

    private void castWithBlackKicker() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{B}"), false);
    }

    private void castWithBothKickers() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{B}"), false);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent findAnavolver() {
        return findPermanent(player1, "Anavolver");
    }
}
