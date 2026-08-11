package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
        Permanent elm = addReadyElm();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
    @DisplayName("Cannot activate without two -1/-1 counters")
    void cannotActivateWithoutTwoCounters() {
        Permanent elm = addReadyElm();
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        forceMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent elm = addReadyElm();
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
        addReadyElm();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElm() {
        Permanent elm = new Permanent(new GnarlbarkElm());
        elm.setSummoningSick(false);
        elm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        gd.playerBattlefields.get(player1.getId()).add(elm);
        return elm;
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
