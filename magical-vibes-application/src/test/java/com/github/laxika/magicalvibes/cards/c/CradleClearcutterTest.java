package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CradleClearcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast makes the tap ability produce mana equal to prototype power")
    void prototypeTapProducesManaEqualToPrototypePower() {
        harness.setHand(player1, List.of(new CradleClearcutter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent clearcutter = findPermanent(player1, "Cradle Clearcutter");
        clearcutter.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability uses the creature's current power")
    void tapUsesCurrentPower() {
        Permanent clearcutter = harness.addToBattlefieldAndReturn(player1, new CradleClearcutter());
        clearcutter.setSummoningSick(false);
        clearcutter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }
}
