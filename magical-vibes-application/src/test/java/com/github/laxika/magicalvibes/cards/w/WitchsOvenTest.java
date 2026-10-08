package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitchsOven.class, GrizzlyBears.class, AirElemental.class, EsixFractalBloom.class})
class WitchsOvenTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature with toughness less than 4 creates one Food token")
    void createsOneFoodForSmallCreature() {
        harness.addToBattlefield(player1, new WitchsOven());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a creature with toughness 4 or greater creates two Food tokens")
    void createsTwoFoodForLargeCreature() {
        harness.addToBattlefield(player1, new WitchsOven());
        addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Food can be sacrificed for two mana to gain three life immediately")
    void createdFoodCanGainLife() {
        harness.addToBattlefield(player1, new WitchsOven());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("A tapped creature that just entered can pay the sacrifice cost")
    void canSacrificeTappedSummoningSickCreatureWithoutMana() {
        Permanent oven = harness.addToBattlefieldAndReturn(player1, new WitchsOven());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(oven.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Counters affect sacrificed toughness while marked damage does not reduce it")
    void usesModifiedToughnessBeforeSacrifice() {
        harness.addToBattlefield(player1, new WitchsOven());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setMarkedDamage(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Esix replaces both Food tokens as a single creation event")
    void largeCreatureCreatesBothTokensInOneEvent() {
        harness.addToBattlefield(player1, new WitchsOven());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player1, new EsixFractalBloom());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertInGraveyard(player1, "Air Elemental");
    }
}
