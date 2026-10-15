package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GarenbrigSquire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaceOfTheValiant.class, GarenbrigSquire.class})
class MaceOfTheValiantTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        mace.setCounterCount(CounterType.CHARGE, 2);
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void creatureEnteringUnderYourControlAddsChargeCounter() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        harness.castFromHand(player1, new GarenbrigSquire(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mace.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void equipAttachesToCreatureYouControl() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void zeroChargeCountersStillGrantVigilanceAndOtherCountersDoNotBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        mace.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        mace.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void opponentCreatureEnteringDoesNotAddCounter() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());

        harness.enterBattlefieldAndReturn(player2, new GarenbrigSquire());

        assertThat(gd.stack).isEmpty();
        assertThat(mace.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void eachCreatureEntryAddsCounterOnlyWhenTriggerResolvesAndUpdatesBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        mace.setAttachedTo(creature.getId());

        harness.enterBattlefieldAndReturn(player1, new GarenbrigSquire());
        harness.enterBattlefieldAndReturn(player1, new GarenbrigSquire());

        assertThat(mace.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(mace.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(mace.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void reequippingMovesBoostAndVigilanceWithoutMovingCounters() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new MaceOfTheValiant());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GarenbrigSquire());
        mace.setCounterCount(CounterType.CHARGE, 2);
        mace.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(mace.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }
}
