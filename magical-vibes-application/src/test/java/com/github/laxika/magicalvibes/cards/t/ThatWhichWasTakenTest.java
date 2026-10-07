package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThatWhichWasTaken.class, GnarledMass.class, TerashisGrasp.class})
class ThatWhichWasTakenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a divinity counter on another permanent")
    void putsDivinityCounterOnAnotherPermanent() {
        Permanent taken = addTaken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(taken.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A divinity counter grants indestructible to that permanent")
    void divinityCounterGrantsIndestructible() {
        Permanent taken = addTaken(player1);
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GnarledMass());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        ownTarget.setCounterCount(CounterType.DIVINITY, 1);
        opposingTarget.setCounterCount(CounterType.DIVINITY, 1);

        assertThat(gqs.hasKeyword(gd, ownTarget, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingTarget, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, taken, Keyword.INDESTRUCTIBLE)).isFalse();

        taken.setCounterCount(CounterType.DIVINITY, 1);

        assertThat(gqs.hasKeyword(gd, taken, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target That Which Was Taken itself")
    void cannotTargetItself() {
        addTaken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can target another That Which Was Taken")
    void canTargetAnotherPermanentWithTheSameName() {
        addTaken(player1);
        Permanent otherTaken = addTaken(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, otherTaken.getId());
        harness.passBothPriorities();

        assertThat(otherTaken.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
    }

    @Test
    void removingDivinityCounterRemovesIndestructible() {
        addTaken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        target.setCounterCount(CounterType.DIVINITY, 1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();

        target.setCounterCount(CounterType.DIVINITY, 0);

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void divinityCounterDoesNotGrantIndestructibleAfterSourceLeaves() {
        Permanent taken = addTaken(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GnarledMass());
        target.setCounterCount(CounterType.DIVINITY, 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, taken.getId());

        harness.assertInGraveyard(player2, "That Which Was Taken");
        assertThat(target.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void divinityCounterProtectsNoncreaturePermanentFromDestruction() {
        addTaken(player1);
        Permanent otherTaken = addTaken(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, otherTaken.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, otherTaken.getId());

        harness.assertOnBattlefield(player2, "That Which Was Taken");
        harness.assertNotInGraveyard(player2, "That Which Was Taken");
        assertThat(otherTaken.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
    }

    private Permanent addTaken(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThatWhichWasTaken());
    }
}
