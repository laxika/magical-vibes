package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdaptiveTrainingPost.class, GrizzlyBears.class, LightningBolt.class, ConeOfFlame.class})
class AdaptiveTrainingPostTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells add charge counters up to three")
    void addsChargeCountersUpToThree() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());

        castLightningBolt();
        castLightningBolt();
        castLightningBolt();
        assertThat(post.getCounterCount(CounterType.CHARGE)).isEqualTo(3);

        castLightningBolt();
        assertThat(post.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature spells do not add charge counters")
    void creatureSpellDoesNotAddChargeCounter() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(post.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Removing three charge counters copies the next instant or sorcery spell")
    void copiesNextInstantOrSorcery() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        post.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(post.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(harness.getGameData().pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId()))
                .isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Removing three charge counters requires three charge counters")
    void copyAbilityNeedsThreeChargeCounters() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        post.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chargeConditionIsCheckedAgainOnResolution() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        post.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        post.setCounterCount(CounterType.CHARGE, 3);
        harness.passBothPriorities();

        assertThat(post.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void opponentsSpellDoesNotAddCounters() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(post.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void separateActivationsCreateSeparateDelayedTriggers() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        post.setCounterCount(CounterType.CHARGE, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getDescription().contains("Copy Lightning Bolt")))
                .hasSize(2);
    }

    @Test
    void sorceryCopyWithMultipleTargetsAllowsChoosingNewTargets() {
        Permanent post = harness.addToBattlefieldAndReturn(player1, new AdaptiveTrainingPost());
        post.setCounterCount(CounterType.CHARGE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ConeOfFlame()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId(), bear.getId()));
        for (int i = 0; i < 2 && !harness.getGameData().stack.isEmpty()
                && harness.getGameData().stack.getLast().getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && !harness.getGameData().interaction.isAwaitingInput(); i++) {
            harness.passBothPriorities();
        }

        assertThat(post.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isTrue();
    }

    private void castLightningBolt() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
