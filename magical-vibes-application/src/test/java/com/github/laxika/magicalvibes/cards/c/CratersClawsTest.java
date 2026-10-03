package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.FleetwheelCruiser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CratersClaws.class, AlpineGrizzly.class, FleetwheelCruiser.class})
class CratersClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage without ferocious")
    void dealsXDamageWithoutFerocious() {
        castCratersClaws(3);

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X plus 2 damage with ferocious")
    void dealsXPlusTwoDamageWithFerocious() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        castCratersClaws(3);

        harness.assertLife(player2, 15);
    }

    @Test
    void zeroXWithoutFerociousDealsNoDamage() {
        castCratersClaws(0);

        harness.assertLife(player2, 20);
    }

    @Test
    void zeroXWithFerociousDealsTwoDamage() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        castCratersClaws(0);

        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new AlpineGrizzly());
        castCratersClaws(3);

        harness.assertLife(player2, 17);
    }

    @Test
    void creatureBelowFourPowerDoesNotEnableFerocious() {
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        creature.setPowerModifier(-1);
        castCratersClaws(3);

        harness.assertLife(player2, 17);
    }

    @Test
    void checksFerociousWhenResolvingRatherThanWhenCast() {
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        prepareCratersClaws(3);
        harness.castSorcery(player1, 0, 3, player2.getId());
        creature.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void gainingFerociousBeforeResolutionAddsTwoDamage() {
        var creature = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        creature.setPowerModifier(-1);
        prepareCratersClaws(3);
        harness.castSorcery(player1, 0, 3, player2.getId());
        creature.setPowerModifier(0);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void canDealDamageToCreatureWithoutFerocious() {
        var target = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        prepareCratersClaws(2);
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertNotOnBattlefield(player2, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Alpine Grizzly");
        harness.assertLife(player2, 20);
    }

    @Test
    void ferociousCanDealLethalDamageToItsOwnEnablingCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        prepareCratersClaws(0);
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player1, "Alpine Grizzly");
    }

    @Test
    void uncrewedVehicleDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new FleetwheelCruiser());
        castCratersClaws(0);

        harness.assertLife(player2, 20);
    }

    private void prepareCratersClaws(int xValue) {
        harness.setHand(player1, List.of(new CratersClaws()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
    }

    private void castCratersClaws(int xValue) {
        prepareCratersClaws(xValue);
        harness.castAndResolveSorcery(player1, 0, xValue, player2.getId());
    }
}
