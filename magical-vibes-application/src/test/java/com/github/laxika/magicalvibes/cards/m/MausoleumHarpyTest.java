package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MausoleumHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature dies with the city's blessing")
    void gainsCounterWhenBlessedCreatureDies() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new MausoleumHarpy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent harpy = findPermanent(player1, "Mausoleum Harpy");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without the city's blessing")
    void doesNotTriggerWithoutBlessing() {
        harness.addToBattlefield(player1, new MausoleumHarpy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent harpy = findPermanent(player1, "Mausoleum Harpy");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Checks the city's blessing when the death trigger occurs")
    void blessingMustExistWhenCreatureDies() {
        harness.addToBattlefield(player1, new MausoleumHarpy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        gd.playersWithCityBlessing.add(player1.getId());
        harness.passBothPriorities();

        Permanent harpy = findPermanent(player1, "Mausoleum Harpy");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
