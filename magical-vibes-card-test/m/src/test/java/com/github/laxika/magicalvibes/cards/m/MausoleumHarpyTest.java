package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MausoleumHarpy.class, GrizzlyBears.class, Shock.class})
class MausoleumHarpyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature dies with the city's blessing")
    void gainsCounterWhenBlessedCreatureDies() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new MausoleumHarpy());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
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
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

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
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        gd.playersWithCityBlessing.add(player1.getId());
        harness.passBothPriorities();

        Permanent harpy = findPermanent(player1, "Mausoleum Harpy");
        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opposing creature dying does not trigger the Harpy")
    void opposingCreatureDeathDoesNotTrigger() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addToBattlefield(player1, new MausoleumHarpy());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Mausoleum Harpy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ascend grants the blessing at ten permanents and keeps it after a creature dies")
    void earnsAndKeepsBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new MausoleumHarpy(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(findPermanent(player1, "Mausoleum Harpy")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Harpy's own death does not trigger its ability")
    void ownDeathDoesNotTrigger() {
        gd.playersWithCityBlessing.add(player1.getId());
        Permanent harpy = harness.addToBattlefieldAndReturn(player1, new MausoleumHarpy());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, harpy.getId());
        harness.castAndResolveInstant(player1, 0, harpy.getId());

        harness.assertInGraveyard(player1, "Mausoleum Harpy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each controlled creature death adds a separate counter")
    void gainsCounterForEachCreatureDeath() {
        gd.playersWithCityBlessing.add(player1.getId());
        Permanent harpy = harness.addToBattlefieldAndReturn(player1, new MausoleumHarpy());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.passBothPriorities();

        assertThat(harpy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
