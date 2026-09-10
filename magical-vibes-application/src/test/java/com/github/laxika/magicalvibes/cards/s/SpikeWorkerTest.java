package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeWorker.class, SkyshroudArcher.class, EnsnaringBridge.class})
class SpikeWorkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two +1/+1 counters")
    void entersWithTwoPlusOneCounters() {
        harness.castFromHand(player1, new SpikeWorker(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spike Worker")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to put one on target creature")
    void removesCounterAndPutsCounterOnTargetCreature() {
        Permanent worker = addWorker(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyshroudArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, worker), null, target.getId());
        harness.passBothPriorities();

        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability resolves after removing the last +1/+1 counter")
    void abilityResolvesAfterRemovingLastCounter() {
        Permanent worker = addWorker(player1);
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyshroudArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, worker), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(worker);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent worker = addWorker(player1);
        Permanent bridge = harness.addToBattlefieldAndReturn(player2, new EnsnaringBridge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, worker), null, bridge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent worker = addWorker(player1);
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyshroudArcher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, worker), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addWorker(Player player) {
        return harness.enterBattlefieldAndReturn(player, new SpikeWorker());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
