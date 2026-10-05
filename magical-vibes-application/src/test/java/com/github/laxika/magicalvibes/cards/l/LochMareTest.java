package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LochMare.class, Forest.class, GrizzlyBears.class})
class LochMareTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three -1/-1 counters")
    void entersWithThreeMinusOneMinusOneCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LochMare()));
        addMana(player1, ManaColor.COLORLESS, 1);
        addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Loch Mare").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Removing one -1/-1 counter draws a card")
    void drawAbilityRemovesOneCounterAndDraws() {
        Permanent lochMare = addReadyLochMare(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 1);
        addMana(player1, ManaColor.BLUE, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Removing two -1/-1 counters taps a creature and puts a stun counter on it")
    void tapAbilityRemovesTwoCountersAndStunsTarget() {
        Permanent lochMare = addReadyLochMare(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 2);
        addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability rejects a noncreature target")
    void tapAbilityRejectsNoncreatureTarget() {
        addReadyLochMare(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMana(player1, ManaColor.COLORLESS, 2);
        addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Draw ability can remove a stun counter while tapped and summoning sick")
    void drawAbilityCanRemoveStunCounter() {
        Permanent lochMare = harness.addToBattlefieldAndReturn(player1, new LochMare());
        lochMare.setSummoningSick(true);
        lochMare.setTapped(true);
        lochMare.setCounterCount(CounterType.STUN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lochMare.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(lochMare.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can remove two counters of different types")
    void tapAbilityCanRemoveDifferentCounterTypes() {
        Permanent lochMare = harness.addToBattlefieldAndReturn(player1, new LochMare());
        lochMare.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        lochMare.setCounterCount(CounterType.STUN, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LochMare());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(lochMare.getCounterCount(CounterType.STUN)).isZero();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped creature still gets a stun counter that replaces its next untap")
    void alreadyTappedTargetReceivesStunCounter() {
        addReadyLochMare(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LochMare());
        target.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Loch Mare can target itself with its tap ability")
    void tapAbilityCanTargetItself() {
        Permanent lochMare = addReadyLochMare(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, lochMare.getId());
        harness.passBothPriorities();

        assertThat(lochMare.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(lochMare.isTapped()).isTrue();
        assertThat(lochMare.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    private Permanent addReadyLochMare(Player player) {
        Permanent lochMare = harness.addToBattlefieldAndReturn(player, new LochMare());
        lochMare.setSummoningSick(false);
        lochMare.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        return lochMare;
    }

    private void addMana(Player player, ManaColor color, int amount) {
        harness.addMana(player, color, amount);
    }
}
