package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MurkfiendLiege;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerieOuphes.class, AirElemental.class, AngelOfMercy.class, LlanowarElves.class, MurkfiendLiege.class})
class AerieOuphesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to its power (3), killing a 3/3 flyer")
    void dealsPowerDamageKillingFlyer() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AngelOfMercy());

        Permanent target = findPermanent(player2, "Angel of Mercy");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Angel of Mercy");
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }

    @Test
    @DisplayName("A 4/4 flyer survives the 3 damage")
    void higherToughnessFlyerSurvives() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AirElemental());

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Aerie Ouphes is sacrificed as a cost of the ability")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AngelOfMercy());

        Permanent target = findPermanent(player2, "Angel of Mercy");
        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Aerie Ouphes");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("Persist returns the sacrificed creature with a -1/-1 counter")
    void persistReturnsWithCounter() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AirElemental());
        Permanent original = findPermanent(player1, "Aerie Ouphes");

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Air Elemental"));
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Aerie Ouphes");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Aerie Ouphes");
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A second sacrifice deals reduced power damage and cannot persist again")
    void secondSacrificeUsesReducedPowerAndStaysInGraveyard() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Angel of Mercy"));
        resolveAllTriggers();
        harness.addToBattlefield(player2, new AirElemental());

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Air Elemental"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aerie Ouphes");
        harness.assertInGraveyard(player1, "Aerie Ouphes");
        assertThat(findPermanent(player2, "Air Elemental").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage uses power including static bonuses before sacrifice")
    void damageIncludesLastKnownStaticPowerBonus() {
        harness.addToBattlefield(player1, new AerieOuphes());
        harness.addToBattlefield(player1, new MurkfiendLiege());
        harness.addToBattlefield(player2, new AirElemental());

        harness.activateAbility(player1, 0, 0, null, harness.getPermanentId(player2, "Air Elemental"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }
}
