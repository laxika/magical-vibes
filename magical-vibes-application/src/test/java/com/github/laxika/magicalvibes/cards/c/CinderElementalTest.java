package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.GideonChampionOfJustice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CinderElemental.class, FreshVolunteers.class, GideonChampionOfJustice.class})
class CinderElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        addCreatureReady(player1, new CinderElemental());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Deals X damage to a target creature, killing it")
    void dealsXDamageToCreature() {
        addCreatureReady(player1, new CinderElemental());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Sacrifices itself as part of the activation cost")
    void sacrificesItselfAsCost() {
        Permanent elemental = addCreatureReady(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, player2.getId());

        assertThat(elemental.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Cinder Elemental");

        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("X=0 still sacrifices itself and deals no damage")
    void zeroXStillSacrificesAndDealsNoDamage() {
        addCreatureReady(player1, new CinderElemental());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cinder Elemental");
        harness.assertLife(player2, 20);
    }

    @Test
    void canDamageItsController() {
        addCreatureReady(player1, new CinderElemental());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Cinder Elemental");
    }

    @Test
    void damagesPlaneswalkerWithoutDamagingItsController() {
        addCreatureReady(player1, new CinderElemental());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonChampionOfJustice());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, 2, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Gideon, Champion of Justice");
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetItselfButDoesNotDealDamageAfterSacrifice() {
        Permanent elemental = addCreatureReady(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, 3, elemental.getId());

        harness.assertInGraveyard(player1, "Cinder Elemental");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cinder Elemental");
        harness.assertNotInGraveyard(player1, "Cinder Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent elemental = addCreatureReady(player1, new CinderElemental());
        elemental.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cinder Elemental");
        harness.assertNotInGraveyard(player1, "Cinder Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresRedManaEvenWhenXIsZero() {
        Permanent elemental = addCreatureReady(player1, new CinderElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(elemental.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Cinder Elemental");
        harness.assertNotInGraveyard(player1, "Cinder Elemental");
        assertThat(gd.stack).isEmpty();
    }
}
