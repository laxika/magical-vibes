package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorozdaGorgon.class, GrizzlyBears.class})
class KorozdaGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Can pay with another creature's counter while tapped and summoning sick")
    void paysWithAnotherCreatureCounter() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        source.tap();
        source.setSummoningSick(true);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KorozdaGorgon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(donor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot pay with a counter on an opponent's creature")
    void cannotUseOpponentCounter() {
        harness.addToBattlefield(player1, new KorozdaGorgon());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KorozdaGorgon());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Repeated activations stack and can target the source")
    void repeatedActivationsCanTargetSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new KorozdaGorgon());
        donor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(donor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, source)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
    }
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
