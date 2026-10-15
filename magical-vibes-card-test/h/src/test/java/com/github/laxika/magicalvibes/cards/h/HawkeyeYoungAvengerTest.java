package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeYoungAvenger.class, GrizzlyBears.class, Shock.class, Humble.class, ChandraNalaar.class})
class HawkeyeYoungAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds Hawkeye's power to noncombat damage dealt to an opponent")
    void addsPowerToNoncombatDamageToOpponent() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(1);
        harness.setLife(player2, 20);
        castShock(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Adds Hawkeye's power to noncombat damage dealt to an opponent's permanent")
    void addsPowerToNoncombatDamageToOpponentPermanent() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShock(bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not add Hawkeye's power to combat damage")
    void doesNotAddPowerToCombatDamage() {
        addCreatureReady(player1, new HawkeyeYoungAvenger());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void castShock(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    void negativePowerReducesDamageToOpponent() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(-3);
        harness.setLife(player2, 20);

        castShock(player2.getId());

        harness.assertLife(player2, 19);
    }

    @Test
    void sufficientlyNegativePowerReducesDamageToZero() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(-5);
        harness.setLife(player2, 20);

        castShock(player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    void negativePowerReducesDamageToOpponentPermanent() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(-3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShock(bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void losingAbilitiesDisablesBonusEvenWithPositivePower() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        hawkeye.setPowerModifier(1);
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, hawkeye.getId());
        harness.setLife(player2, 20);

        castShock(player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    void doesNotIncreaseDamageToController() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        harness.setLife(player1, 20);

        castShock(player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void doesNotIncreaseDamageToOwnPermanent() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());

        castShock(hawkeye.getId());

        harness.assertOnBattlefield(player1, "Hawkeye, Young Avenger");
        assertThat(hawkeye.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void doesNotIncreaseDamageFromOpponentSource() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    void usesPowerWhenDamageIsDealtRatherThanWhenSpellIsCast() {
        Permanent hawkeye = harness.addToBattlefieldAndReturn(player1, new HawkeyeYoungAvenger());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        hawkeye.setPowerModifier(2);

        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void bonusKillsOpponentCreatureThatWouldSurviveUnmodifiedDamage() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setToughnessModifier(1);

        castShock(bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void increasesDamageToOpponentPlaneswalker() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        castShock(chandra.getId());

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    void increasesNoncombatDamageFromPermanentAbility() {
        harness.addToBattlefield(player1, new HawkeyeYoungAvenger());
        harness.addToBattlefield(player1, new ChandraNalaar());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }
}
