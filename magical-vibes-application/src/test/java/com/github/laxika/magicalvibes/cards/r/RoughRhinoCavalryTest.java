package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoughRhinoCavalry.class})
class RoughRhinoCavalryTest extends BaseCardTest {

    @Test
    void attackingAddsTwoRedManaUntilEndOfCombat() {
        addReadyRhino();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void exhaustAddsCountersAndTrampleUntilEndOfTurn() {
        Permanent rhino = addReadyRhino();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isFalse();
        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void exhaustCanBeActivatedOnlyOnce() {
        addReadyRhino();
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void exhaustCannotBeActivatedAgainWhileItsFirstActivationIsOnTheStack() {
        Permanent rhino = addReadyRhino();
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();

        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void separateCopiesCanEachActivateExhaust() {
        Permanent first = addReadyRhino();
        Permanent second = addReadyRhino();
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void exhaustCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new RoughRhinoCavalry());
        rhino.setSummoningSick(true);
        rhino.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(rhino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rhino, Keyword.TRAMPLE)).isTrue();
        assertThat(rhino.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyRhino() {
        return addCreatureReady(player1, new RoughRhinoCavalry());
    }
}
