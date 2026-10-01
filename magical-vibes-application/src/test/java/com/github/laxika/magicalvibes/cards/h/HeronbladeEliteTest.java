package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeronbladeElite.class, EliteVanguard.class, GrizzlyBears.class})
class HeronbladeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another Human enters the battlefield")
    void getsCounterWhenHumanEnters() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new HeronbladeElite());

        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elite)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Human creature enters")
    void noCounterWhenNonHumanEnters() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new HeronbladeElite());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Adds mana of one chosen color equal to its power")
    void addsManaEqualToPower() {
        Permanent elite = addCreatureReady(player1, new HeronbladeElite());
        elite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(elite.isTapped()).isTrue();
    }
}
