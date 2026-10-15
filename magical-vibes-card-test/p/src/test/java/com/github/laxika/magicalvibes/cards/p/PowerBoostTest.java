package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerBoost.class, GrizzlyBears.class, Opalescence.class})
class PowerBoostTest extends BaseCardTest {

    @Test
    void bonusBeginsWhenEnchantmentResolvesAndAppliesToLaterCreatures() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new PowerBoost(), "{2}{R}");

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(2);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Power Boost");
        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(3);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2).allSatisfy(creature -> {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        });
    }

    @Test
    void bonusesFromMultipleCopiesStack() {
        harness.addToBattlefield(player1, new PowerBoost());
        harness.addToBattlefield(player1, new PowerBoost());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void animatedPowerBoostReceivesItsOwnBonus() {
        Permanent powerBoost = harness.addToBattlefieldAndReturn(player1, new PowerBoost());
        Permanent opposingPowerBoost = harness.addToBattlefieldAndReturn(player2, new PowerBoost());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, powerBoost)).isTrue();
        assertThat(gqs.getEffectivePower(gd, powerBoost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, powerBoost)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, opposingPowerBoost)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingPowerBoost)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingPowerBoost)).isEqualTo(3);
    }

    @Test
    void boostsCreaturesYouControlByOnePower() {
        harness.addToBattlefield(player1, new PowerBoost());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void bonusDisappearsWhenPowerBoostLeavesBattlefield() {
        Permanent powerBoost = harness.addToBattlefieldAndReturn(player1, new PowerBoost());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(powerBoost);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }
}
