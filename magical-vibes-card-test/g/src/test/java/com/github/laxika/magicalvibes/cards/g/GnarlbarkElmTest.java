package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GnarlbarkElm.class, Forest.class})
class GnarlbarkElmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters")
    void entersWithMinusOneMinusOneCounters() {
        harness.setHand(player1, List.of(new GnarlbarkElm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elm = findPermanent(player1, "Gnarlbark Elm");

        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes two counters and gives a target creature -2/-2")
    void removesCountersAndDebuffsTargetCreature() {
        Permanent elm = addReadyElm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlbarkElm());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("The debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        addReadyElm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlbarkElm());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate with fewer than two counters")
    void cannotActivateWithoutTwoCounters() {
        Permanent elm = addReadyElm();
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        addReadyElm();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void cannotActivateOnOpponentsTurn() {
        Permanent elm = addReadyElm();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elm.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElm() {
        Permanent elm = addCreatureReady(player1, new GnarlbarkElm());
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return elm;
    }

    @Test
    @DisplayName("Can pay the cost with two +1/+1 counters")
    void canRemovePlusOnePlusOneCounters() {
        Permanent elm = addReadyElm();
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        elm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, elm.getId());

        assertThat(elm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(elm.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(elm.getPowerModifier()).isEqualTo(-2);
        assertThat(elm.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Can pay with counters of different types")
    void canRemoveMixedCounters() {
        Permanent elm = addReadyElm();
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        elm.setCounterCount(CounterType.STUN, 1);
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, elm.getId());

        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(elm.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();
        assertThat(elm.getPowerModifier()).isEqualTo(-2);
        assertThat(elm.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Counters are paid immediately and summoning sickness does not prevent activation")
    void canActivateWhileSummoningSickAndTargetItself() {
        Permanent elm = harness.enterBattlefieldAndReturn(player1, new GnarlbarkElm());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, elm.getId());

        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(elm.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(elm.getPowerModifier()).isEqualTo(-2);
        assertThat(elm.getToughnessModifier()).isEqualTo(-2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elm);
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringCombat() {
        Permanent elm = addReadyElm();
        forceMainPhase();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elm.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate with a nonempty stack")
    void cannotActivateInResponse() {
        addReadyElm();
        Permanent elm = addReadyElm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarlbarkElm());
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
