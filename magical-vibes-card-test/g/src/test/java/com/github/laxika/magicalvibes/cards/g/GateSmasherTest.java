package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.r.ReduceInStature;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GateSmasher.class, AzureDrake.class, GrizzlyBears.class, ReduceInStature.class})
class GateSmasherTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new AzureDrake());
        Permanent gateSmasher = addCreatureReady(player1, new GateSmasher());
        gateSmasher.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipAttachesToCreatureWithToughnessFourOrGreater() {
        Permanent gateSmasher = addCreatureReady(player1, new GateSmasher());
        Permanent creature = addCreatureReady(player1, new AzureDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gateSmasher.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetCreatureWithToughnessLessThanFour() {
        Permanent gateSmasher = addCreatureReady(player1, new GateSmasher());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("toughness 4 or greater");

        assertThat(gateSmasher.getAttachedTo()).isNull();
    }

    @Test
    void becomesUnattachedWhenEquippedCreaturesToughnessDropsBelowFour() {
        Permanent gateSmasher = harness.addToBattlefieldAndReturn(player1, new GateSmasher());
        Permanent creature = addCreatureReady(player1, new AzureDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gateSmasher.getAttachedTo()).isEqualTo(creature.getId());

        harness.setHand(player1, List.of(new ReduceInStature()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gateSmasher.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gateSmasher, creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equipUsesEffectiveToughnessIncludingCounters() {
        Permanent gateSmasher = harness.addToBattlefieldAndReturn(player1, new GateSmasher());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gateSmasher.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void equipDoesNotMoveEquipmentWhenTargetLosesSufficientToughnessBeforeResolution() {
        Permanent gateSmasher = harness.addToBattlefieldAndReturn(player1, new GateSmasher());
        Permanent originalCreature = addCreatureReady(player1, new AzureDrake());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gateSmasher.setAttachedTo(originalCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gateSmasher.getAttachedTo()).isEqualTo(originalCreature.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void equipCannotTargetOpponentsCreatureEvenWithSufficientToughness() {
        Permanent gateSmasher = harness.addToBattlefieldAndReturn(player1, new GateSmasher());
        Permanent creature = addCreatureReady(player2, new AzureDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gateSmasher.getAttachedTo()).isNull();
    }
}
