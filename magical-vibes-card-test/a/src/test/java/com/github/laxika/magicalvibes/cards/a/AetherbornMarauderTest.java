package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherbornMarauder.class, Forest.class, PrakhataPillarBug.class, AnimationModule.class})
class AetherbornMarauderTest extends BaseCardTest {

    @Test
    @DisplayName("Moves chosen +1/+1 counters from any other permanents you control")
    void movesCountersFromMultipleControlledPermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new PrakhataPillarBug());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        cast();
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        Permanent marauder = findPermanent(player1, "Aetherborn Marauder");
        assertThat(marauder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Moving zero counters leaves the other permanent unchanged")
    void mayMoveZeroCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast();
        harness.handleListChoice(player1, "0");

        Permanent marauder = findPermanent(player1, "Aetherborn Marauder");
        assertThat(marauder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new AetherbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Moving counters from multiple permanents is one counter-placement event")
    void movesCountersSimultaneously() {
        harness.addToBattlefield(player1, new AnimationModule());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        cast();
        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(findPermanent(player1, "Aetherborn Marauder")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolves without a choice when no other permanent has +1/+1 counters")
    void ignoresOtherCounterTypes() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 2);

        cast();

        assertThat(findPermanent(player1, "Aetherborn Marauder")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
