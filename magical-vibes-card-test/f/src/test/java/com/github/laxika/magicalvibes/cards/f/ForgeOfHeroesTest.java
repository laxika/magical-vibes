package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForgeOfHeroes.class, GrizzlyBears.class, ChandraNalaar.class})
class ForgeOfHeroesTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new ForgeOfHeroes());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void putsPlusOneCounterOnCommanderCreatureThatEnteredThisTurn() {
        harness.addToBattlefield(player1, new ForgeOfHeroes());
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.enterBattlefieldAndReturn(player1, commander);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    void putsLoyaltyCounterOnCommanderPlaneswalkerThatEnteredThisTurn() {
        harness.addToBattlefield(player1, new ForgeOfHeroes());
        Card commander = new ChandraNalaar();
        gd.makeCommander(player1.getId(), commander);
        Permanent target = harness.enterBattlefieldAndReturn(player1, commander);
        target.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void onlyTargetsCommandersThatEnteredThisTurn() {
        harness.addToBattlefield(player1, new ForgeOfHeroes());
        Permanent noncommander = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncommander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("commander");

        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        Permanent oldCommander = harness.addToBattlefieldAndReturn(player1, commander);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, oldCommander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered the battlefield this turn");
    }
}
