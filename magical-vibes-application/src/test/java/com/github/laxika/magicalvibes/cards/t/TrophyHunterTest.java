package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BurningPalmEfreet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrophyHunter.class, BurningPalmEfreet.class, GrizzlyBears.class, SuntailHawk.class, WindDrake.class})
class TrophyHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a flying creature and gets a +1/+1 counter when it dies")
    void damagesFlyingCreatureAndGainsCounterWhenItDies() {
        Permanent hunter = addReady(player1, new TrophyHunter());
        Permanent target = addReady(player2, new SuntailHawk());

        activateHunter(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Does not get a counter when the damaged flying creature survives")
    void noCounterWhenTargetSurvives() {
        Permanent hunter = addReady(player1, new TrophyHunter());
        Permanent target = addReady(player2, new WindDrake());

        activateHunter(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isOne();
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Checks flying when the damaged creature dies")
    void noCounterWhenTargetLosesFlyingBeforeItDies() {
        Permanent hunter = addReady(player1, new TrophyHunter());
        addReady(player1, new BurningPalmEfreet());
        Permanent target = addReady(player2, new WindDrake());

        activateHunter(target);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        addReady(player1, new TrophyHunter());
        Permanent target = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    private void activateHunter(Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
