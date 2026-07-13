package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TumbleMagnet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChainbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (3/3 becomes 1/1)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Chainbreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB processing

        Permanent chainbreaker = findPermanent(player1, "Chainbreaker");
        assertThat(chainbreaker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(chainbreaker.getEffectivePower()).isEqualTo(1);
        assertThat(chainbreaker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability removes a -1/-1 counter from target creature")
    void removesCounterFromTarget() {
        addReadyChainbreaker(player1);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent giant = findPermanent(player1, "Hill Giant");
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyChainbreaker(player1);
        harness.addToBattlefield(player2, new TumbleMagnet());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Permanent magnet = findPermanent(player2, "Tumble Magnet");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, magnet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyChainbreaker(player1);
        harness.addToBattlefield(player1, new HillGiant());

        Permanent giant = findPermanent(player1, "Hill Giant");
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyChainbreaker(Player player) {
        Permanent chainbreaker = harness.addToBattlefieldAndReturn(player, new Chainbreaker());
        chainbreaker.setSummoningSick(false);
    }
}
