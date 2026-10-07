package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoxDiamond;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeSoldier.class, SkyshroudTroopers.class, MoxDiamond.class})
class SpikeSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new SpikeSoldier()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spike Soldier")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability removes a counter and puts one on target creature")
    void firstAbilityMovesCounterToTargetCreature() {
        Permanent spike = addReadySpike(player1, 3);
        Permanent troopers = harness.addToBattlefieldAndReturn(player2, new SkyshroudTroopers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 0, null, troopers.getId());
        harness.passBothPriorities();

        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(troopers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability resolves after removing the last counter and the source dies")
    void firstAbilityResolvesAfterRemovingLastCounter() {
        Permanent spike = addReadySpike(player1, 1);
        Permanent troopers = harness.addToBattlefieldAndReturn(player2, new SkyshroudTroopers());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 0, null, troopers.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spike);
        assertThat(troopers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can be activated while Spike Soldier is summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent spike = harness.enterBattlefieldAndReturn(player1, new SpikeSoldier());
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 1, null, null);
        harness.passBothPriorities();

        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability removes a counter and gives Spike Soldier +2/+2 until end of turn")
    void secondAbilityBoostsSelfUntilEndOfTurn() {
        Permanent spike = addReadySpike(player1, 3);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 1, null, null);
        harness.passBothPriorities();

        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spike)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spike)).isEqualTo(4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, spike)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spike)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability cannot target a noncreature permanent")
    void firstAbilityCannotTargetNoncreature() {
        Permanent spike = addReadySpike(player1, 3);
        Permanent moxDiamond = harness.addToBattlefieldAndReturn(player2, new MoxDiamond());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, spike), 0, null, moxDiamond.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Neither ability can be activated without a +1/+1 counter")
    void cannotActivateWithoutCounter() {
        Permanent spike = addReadySpike(player1, 3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyshroudTroopers());
        prepareMainPhase(player1);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, battlefieldIndex(player1, spike), 1, null, null);
            harness.passBothPriorities();
        }
        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, spike)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, spike)).isEqualTo(6);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, spike), 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, spike), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Counter transfer can target Spike Soldier itself and pays its cost immediately")
    void firstAbilityCanTargetSelf() {
        Permanent spike = addReadySpike(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 0, null, spike.getId());

        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(spike.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the final counter for a boost kills an unboosted Spike before resolution")
    void finalCounterBoostCannotSaveSource() {
        Permanent spike = addReadySpike(player1, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, spike), 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spike Soldier");
        harness.assertInGraveyard(player1, "Spike Soldier");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySpike(Player player, int counters) {
        Permanent spike = addCreatureReady(player, new SpikeSoldier());
        spike.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return spike;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
