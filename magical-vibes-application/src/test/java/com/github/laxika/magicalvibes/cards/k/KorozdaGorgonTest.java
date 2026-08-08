package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KorozdaGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a +1/+1 counter and gives target creature -1/-1")
    void givesTargetMinusOneMinusOne() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        gorgon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        gorgon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot be activated when no creature you control has a +1/+1 counter")
    void cannotActivateWithoutCounters() {
        harness.addToBattlefield(player1, new KorozdaGorgon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated without enough mana")
    void requiresMana() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        gorgon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
