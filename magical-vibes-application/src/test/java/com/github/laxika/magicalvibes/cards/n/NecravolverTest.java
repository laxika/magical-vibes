package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Necravolver.class)
class NecravolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Necravolver enters without counters or trample")
    void withoutKickers() {
        Permanent necravolver = castNecravolver();

        assertThat(necravolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, necravolver, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Green kicker adds two counters and trample")
    void greenKicker() {
        Permanent necravolver = castWithGreenKicker();

        assertThat(necravolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, necravolver, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("White kicker adds one counter and gains life from damage")
    void whiteKicker() {
        Permanent necravolver = castWithWhiteKicker();

        assertThat(necravolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, necravolver, Keyword.TRAMPLE)).isFalse();

        prepareAttacker(necravolver);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        resolveCombatAndTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Both kickers apply both sets of effects")
    void bothKickers() {
        Permanent necravolver = castWithBothKickers();

        assertThat(necravolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, necravolver, Keyword.TRAMPLE)).isTrue();

        prepareAttacker(necravolver);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        resolveCombatAndTrigger();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    private Permanent castNecravolver() {
        addMana(ManaColor.COLORLESS, 2);
        addMana(ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Necravolver()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findNecravolver();
    }

    private Permanent castWithGreenKicker() {
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Necravolver()));
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        return findNecravolver();
    }

    private Permanent castWithWhiteKicker() {
        addMana(ManaColor.COLORLESS, 2);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new Necravolver()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{W}"), false);
        harness.passBothPriorities();
        return findNecravolver();
    }

    private Permanent castWithBothKickers() {
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.BLACK, 1);
        addMana(ManaColor.GREEN, 1);
        addMana(ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new Necravolver()));
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{W}"), false);
        harness.passBothPriorities();
        return findNecravolver();
    }

    private void prepareAttacker(Permanent necravolver) {
        necravolver.setSummoningSick(false);
        necravolver.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }

    private Permanent findNecravolver() {
        return findPermanent(player1, "Necravolver");
    }
}
