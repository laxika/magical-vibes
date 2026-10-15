package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CaptainMarvelEarthsProtector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WonderManHollywoodHero.class, CaptainMarvelEarthsProtector.class})
class WonderManHollywoodHeroTest extends BaseCardTest {

    @Test
    void letsEachControlledPowerUpAbilityBeActivatedTwice() {
        Permanent wonderMan = harness.enterBattlefieldAndReturn(player1, new WonderManHollywoodHero());
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(wonderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotActivateHisOwnPowerUpAThirdTime() {
        Permanent wonderMan = harness.enterBattlefieldAndReturn(player1, new WonderManHollywoodHero());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wonderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void doesNotGrantAnExtraActivationToAnOpponent() {
        harness.enterBattlefieldAndReturn(player1, new WonderManHollywoodHero());
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player2, new CaptainMarvelEarthsProtector());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void paysTheFullPowerUpCostWhenHeDidNotEnterThisTurn() {
        Permanent wonderMan = harness.addToBattlefieldAndReturn(player1, new WonderManHollywoodHero());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wonderMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void replacingWonderManDoesNotResetAnotherPermanentsActivationLimit() {
        harness.enterBattlefieldAndReturn(player1, new WonderManHollywoodHero());
        Permanent captainMarvel = harness.enterBattlefieldAndReturn(player1, new CaptainMarvelEarthsProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.enterBattlefieldAndReturn(player1, new WonderManHollywoodHero());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(captainMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
