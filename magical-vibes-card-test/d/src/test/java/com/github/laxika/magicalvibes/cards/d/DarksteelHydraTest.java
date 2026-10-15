package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelHydra.class, DarksteelIngot.class, DarksteelPlate.class})
class DarksteelHydraTest extends BaseCardTest {

    @Test
    void entersWithXOilCountersAndScalesPowerAndToughnessToTwiceThatNumber() {
        Permanent hydra = castHydraForX(3);

        assertThat(hydra.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(6);

        hydra.setCounterCount(CounterType.OIL, 5);

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(10);
    }

    @Test
    void conjuresDarksteelIngotAndDarksteelPlateIntoHand() {
        castHydraForX(1);

        harness.assertInHand(player1, "Darksteel Ingot");
        harness.assertInHand(player1, "Darksteel Plate");
    }

    @Test
    void zeroXDiesDespiteIndestructibleAndStillConjuresBothCards() {
        castHydra(0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Darksteel Hydra");
        harness.assertInGraveyard(player1, "Darksteel Hydra");
        harness.assertInHand(player1, "Darksteel Ingot");
        harness.assertInHand(player1, "Darksteel Plate");
    }

    @Test
    void oilCountersArePresentBeforeTheConjureTriggerResolves() {
        castHydra(2);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Darksteel Hydra");
        assertThat(hydra.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(4);
        harness.assertNotInHand(player1, "Darksteel Ingot");
        harness.assertNotInHand(player1, "Darksteel Plate");

        resolveAllTriggers();
        harness.assertInHand(player1, "Darksteel Ingot");
        harness.assertInHand(player1, "Darksteel Plate");
        harness.assertNotInHand(player2, "Darksteel Ingot");
        harness.assertNotInHand(player2, "Darksteel Plate");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void otherCounterTypesDoNotCountAsOilAndCounterBonusesApplyAfterBasePowerAndToughness() {
        Permanent hydra = castHydraForX(3);
        hydra.setCounterCount(CounterType.CHARGE, 5);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        hydra.setCounterCount(CounterType.OIL, 1);

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(3);
    }

    private Permanent castHydraForX(int xValue) {
        castHydra(xValue);
        resolveAllTriggers();
        return findPermanent(player1, "Darksteel Hydra");
    }

    private void castHydra(int xValue) {
        harness.setHand(player1, List.of(new DarksteelHydra()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castCreature(player1, 0, xValue);
    }
}
