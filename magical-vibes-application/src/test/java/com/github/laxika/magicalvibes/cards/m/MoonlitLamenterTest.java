package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MoonlitLamenter.class})
class MoonlitLamenterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a -1/-1 counter")
    void entersWithMinusOneMinusOneCounter() {
        harness.setHand(player1, List.of(new MoonlitLamenter()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent lamenter = findPermanent(player1, "Moonlit Lamenter");

        assertThat(lamenter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing its counter draws a card")
    void removesCounterAndDrawsCard() {
        Permanent lamenter = harness.addToBattlefieldAndReturn(player1, new MoonlitLamenter());
        lamenter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(lamenter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate without a counter")
    void cannotActivateWithoutCounter() {
        Permanent lamenter = harness.addToBattlefieldAndReturn(player1, new MoonlitLamenter());
        lamenter.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Can activate only at sorcery speed")
    void cannotActivateOnOpponentsTurn() {
        Permanent lamenter = harness.enterBattlefieldAndReturn(player1, new MoonlitLamenter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(lamenter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void paysEntryCounterBeforeDrawingEvenWhenTappedAndSummoningSick() {
        Permanent lamenter = harness.enterBattlefieldAndReturn(player1, new MoonlitLamenter());
        lamenter.setTapped(true);
        lamenter.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new MoonlitLamenter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lamenter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(lamenter.isTapped()).isTrue();
    }

    @Test
    void canRemoveAStunCounterToDraw() {
        Permanent lamenter = harness.addToBattlefieldAndReturn(player1, new MoonlitLamenter());
        lamenter.setCounterCount(CounterType.STUN, 1);
        harness.setLibrary(player1, List.of(new MoonlitLamenter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(lamenter.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent lamenter = harness.enterBattlefieldAndReturn(player1, new MoonlitLamenter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(lamenter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileAnotherSpellIsOnStack() {
        Permanent lamenter = harness.enterBattlefieldAndReturn(player1, new MoonlitLamenter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MoonlitLamenter()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(lamenter.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }
}
