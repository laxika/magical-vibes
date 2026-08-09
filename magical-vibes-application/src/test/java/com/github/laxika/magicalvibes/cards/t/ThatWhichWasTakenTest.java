package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThatWhichWasTakenTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a divinity counter on another permanent")
    void putsDivinityCounterOnAnotherPermanent() {
        Permanent taken = addTaken(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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

    private Permanent addTaken(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ThatWhichWasTaken());
    }
}
