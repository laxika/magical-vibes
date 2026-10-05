package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkmossBridge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrScrapling.class, DarkmossBridge.class})
class MyrScraplingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it puts a +1/+1 counter on target creature")
    void sacrificesAndPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MyrScrapling());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MyrScrapling());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        harness.assertInGraveyard(player1, "Myr Scrapling");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new MyrScrapling());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DarkmossBridge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Myr Scrapling");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Scrapling can put a counter on an opponent's creature")
    void canActivateWhileTappedAndSummoningSickTargetingOpponent() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MyrScrapling());
        source.setTapped(true);
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MyrScrapling());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Myr Scrapling");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("It can target itself, but sacrificing it makes the target illegal")
    void canTargetItselfButDoesNotPutCounterOnSacrificedSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MyrScrapling());

        harness.activateAbility(player1, 0, null, source.getId());

        harness.assertNotOnBattlefield(player1, "Myr Scrapling");
        harness.assertInGraveyard(player1, "Myr Scrapling");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A target is required before the sacrifice cost can be paid")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new MyrScrapling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Myr Scrapling");
        harness.assertNotInGraveyard(player1, "Myr Scrapling");
        assertThat(gd.stack).isEmpty();
    }
}
