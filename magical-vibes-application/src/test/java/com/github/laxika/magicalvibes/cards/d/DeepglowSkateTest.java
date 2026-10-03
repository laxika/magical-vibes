package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeepglowSkate.class, GrizzlyBears.class, FountainOfYouth.class})
class DeepglowSkateTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles every kind of counter on any number of target permanents")
    void doublesCountersOnMultiplePermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.CHARGE, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enter without choosing any targets")
    void canChooseNoTargets() {
        harness.castFromHand(player1, new DeepglowSkate(), "{4}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deepglow Skate");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Any number of targets includes more than ninety-nine permanents")
    void canTargetOneHundredPermanents() {
        List<Permanent> targets = new ArrayList<>();
        List<UUID> targetIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
            target.setCounterCount(CounterType.CHARGE, 1);
            targets.add(target);
            targetIds.add(target.getId());
        }
        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(targets).allSatisfy(target ->
                assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2));
    }

    @Test
    @DisplayName("Uses each kind of counter present when the trigger resolves")
    void doublesCountersPresentAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        unselected.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        target.setCounterCount(CounterType.CHARGE, 4);
        target.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
        assertThat(target.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unselected.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Still doubles a legal target when another target leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        departed.setCounterCount(CounterType.CHARGE, 2);
        remaining.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of(new DeepglowSkate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(departed.getId(), remaining.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(departed);
        gd.playerGraveyards.get(player2.getId()).add(departed.getCard());
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        assertThat(departed.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }
}
