package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElsewhereFlask;
import com.github.laxika.magicalvibes.cards.w.WickerWarcrawler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chainbreaker.class, WickerWarcrawler.class, ElsewhereFlask.class})
class ChainbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (3/3 becomes 1/1)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Chainbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent chainbreaker = findPermanent(player1, "Chainbreaker");
        assertThat(chainbreaker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(chainbreaker.getEffectivePower()).isEqualTo(1);
        assertThat(chainbreaker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability removes a -1/-1 counter from target creature")
    void removesCounterFromTarget() {
        addCreatureReady(player1, new Chainbreaker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent giant = harness.addToBattlefieldAndReturn(player1, new WickerWarcrawler());
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new Chainbreaker());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent magnet = harness.addToBattlefieldAndReturn(player2, new ElsewhereFlask());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, magnet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new Chainbreaker());

        Permanent giant = harness.addToBattlefieldAndReturn(player1, new WickerWarcrawler());
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canRemoveItsOwnCounterAndPaysTapCost() {
        Permanent chainbreaker = addCreatureReady(player1, new Chainbreaker());
        chainbreaker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, chainbreaker.getId());

        assertThat(chainbreaker.isTapped()).isTrue();
        assertThat(chainbreaker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(chainbreaker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void canRemoveCounterFromOpponentsCreature() {
        addCreatureReady(player1, new Chainbreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WickerWarcrawler());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void creatureWithoutMinusCountersIsLegalAndOtherCountersAreUnaffected() {
        Permanent chainbreaker = addCreatureReady(player1, new Chainbreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WickerWarcrawler());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(chainbreaker.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent chainbreaker = harness.addToBattlefieldAndReturn(player1, new Chainbreaker());
        chainbreaker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, chainbreaker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent chainbreaker = addCreatureReady(player1, new Chainbreaker());
        chainbreaker.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, chainbreaker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        Permanent chainbreaker = addCreatureReady(player1, new Chainbreaker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, chainbreaker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingIfLastMinusCounterIsRemovedBeforeResolution() {
        addCreatureReady(player1, new Chainbreaker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WickerWarcrawler());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Wicker Warcrawler");
        harness.assertLife(player1, 20);
    }
}
