package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.k.KuroPitlord;
import com.github.laxika.magicalvibes.cards.n.NoDachi;
import com.github.laxika.magicalvibes.cards.o.OrderOfTheSacredBell;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodthirstyOgre.class, KuroPitlord.class, NoDachi.class, OrderOfTheSacredBell.class,
        WanderingOnes.class})
class BloodthirstyOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability puts a devotion counter on itself")
    void tapPutsDevotionCounter() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, ogre), 0, null, null);
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.DEVOTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap cost prevents activating the other ability while tapped")
    void cannotActivateWhileTapped() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrderOfTheSacredBell());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, ogre), 0, null, null);
        harness.passBothPriorities();

        assertThat(ogre.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With a Demon, target gets -X/-X equal to devotion counters")
    void debuffScalesWithDevotionCounters() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrderOfTheSacredBell());
        forceMainPhase(player1);

        putDevotionCounters(ogre, 2);

        harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Enough devotion counters kill the target")
    void enoughCountersKillTarget() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        forceMainPhase(player1);

        putDevotionCounters(ogre, 2);

        harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("With no devotion counters, the debuff is -0/-0")
    void zeroDevotionCountersHaveNoEffect() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NoDachi());
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOff() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player1, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrderOfTheSacredBell());
        forceMainPhase(player1);

        putDevotionCounters(ogre, 1);

        harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate the debuff ability without controlling a Demon")
    void cannotActivateWithoutDemon() {
        Permanent ogre = addCreatureReady(player1, new BloodthirstyOgre());
        harness.addToBattlefield(player2, new KuroPitlord());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        forceMainPhase(player1);

        putDevotionCounters(ogre, 2);

        int index = indexOf(player1, ogre);
        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void putDevotionCounters(Permanent ogre, int count) {
        ogre.setCounterCount(CounterType.DEVOTION, count);
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
