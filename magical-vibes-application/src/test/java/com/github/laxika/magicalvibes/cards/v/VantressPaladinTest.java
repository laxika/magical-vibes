package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VantressPaladin.class})
class VantressPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when at least three blue mana is spent")
    void entersWithCounterWhenThreeBlueManaIsSpent() {
        harness.castFromHand(player1, new VantressPaladin(), "{1}{U}{U}{U}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Vantress Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when fewer than three blue mana is spent")
    void doesNotEnterWithCounterWhenFewerThanThreeBlueManaIsSpent() {
        harness.castFromHand(player1, new VantressPaladin(), "{3}{U}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Vantress Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exactly two blue mana does not satisfy adamant")
    void doesNotEnterWithCounterWhenTwoBlueManaIsSpent() {
        harness.castFromHand(player1, new VantressPaladin(), "{2}{U}{U}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Vantress Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Spending four blue mana still gives exactly one counter")
    void entersWithOneCounterWhenFourBlueManaIsSpent() {
        harness.castFromHand(player1, new VantressPaladin(), "{U}{U}{U}{U}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Vantress Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast does not give an adamant counter")
    void doesNotEnterWithCounterWithoutBeingCast() {
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent paladin = harness.enterBattlefieldAndReturn(player1, new VantressPaladin());

        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
