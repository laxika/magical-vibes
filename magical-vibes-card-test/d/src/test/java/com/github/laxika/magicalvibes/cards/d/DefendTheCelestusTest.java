package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendTheCelestus.class, DawnhartRejuvenator.class})
class DefendTheCelestusTest extends BaseCardTest {

    @Test
    void putsAllThreeCountersOnOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        harness.castInstant(player1, 0, Map.of(target.getId(), 3));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Defend the Celestus");
    }

    @Test
    void rejectsAnEmptyDistribution() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addToBattlefield(player1, new DawnhartRejuvenator());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsDistributionOfFewerThanThreeCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsATargetAssignedZeroCounters() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(first.getId(), 3, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRedistributeCountersWhenATargetLeavesTheBattlefield() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        harness.castInstant(player1, 0, Map.of(leaving.getId(), 2, remaining.getId(), 1));
        gd.playerBattlefields.get(player1.getId()).remove(leaving);
        gd.playerHands.get(player1.getId()).add(leaving.getCard());
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leaving.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Defend the Celestus");
    }

    @Test
    void doesNotPutCountersOnATargetThatChangesToOpponentsControl() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        harness.castInstant(player1, 0, Map.of(stolen.getId(), 2, remaining.getId(), 1));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), stolen,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.passBothPriorities();

        assertThat(stolen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Defend the Celestus");
    }

    @Test
    void distributesOneCounterToEachOfThreeTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent bears3 = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        harness.castInstant(player1, 0, Map.of(
                bears1.getId(), 1,
                bears2.getId(), 1,
                bears3.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears3.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void allowsUnevenDistributionAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new DawnhartRejuvenator());

        harness.castInstant(player1, 0, Map.of(bears1.getId(), 2, bears2.getId(), 1));
        harness.passBothPriorities();

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rejectsCreatureControlledByOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(opponentBears.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
