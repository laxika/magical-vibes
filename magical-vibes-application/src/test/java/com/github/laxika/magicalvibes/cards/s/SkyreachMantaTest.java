package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyreachManta.class})
class SkyreachMantaTest extends BaseCardTest {

    @Test
    void sunburstPutsOneCounterForEachColorSpent() {
        harness.setHand(player1, List.of(new SkyreachManta()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent manta = findPermanent(player1, "Skyreach Manta");
        assertThat(manta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void sunburstCountsEachColorOnlyOnce() {
        harness.setHand(player1, List.of(new SkyreachManta()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent manta = findPermanent(player1, "Skyreach Manta");
        assertThat(manta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sunburstIgnoresColorlessMana() {
        harness.setHand(player1, List.of(new SkyreachManta()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent manta = findPermanent(player1, "Skyreach Manta");
        assertThat(manta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entirelyColorlessPaymentLeavesMantaWithZeroToughness() {
        harness.setHand(player1, List.of(new SkyreachManta()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyreach Manta");
        harness.assertInGraveyard(player1, "Skyreach Manta");
    }

    @Test
    void enteringWithoutBeingCastDoesNotUseManaInPool() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent manta = harness.enterBattlefieldAndReturn(player1, new SkyreachManta());
        assertThat(manta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Skyreach Manta");
        harness.assertInGraveyard(player1, "Skyreach Manta");
    }

    @Test
    void manaAddedAfterCastingDoesNotChangeSunburst() {
        harness.setHand(player1, List.of(new SkyreachManta()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passBothPriorities();

        Permanent manta = findPermanent(player1, "Skyreach Manta");
        assertThat(manta.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
