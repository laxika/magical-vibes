package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DemonOfDeathsGate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodthirstyOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability puts a devotion counter on itself")
    void tapPutsDevotionCounter() {
        Permanent ogre = addReadyOgre();
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, ogre), 0, null, null);
        harness.passBothPriorities();

        assertThat(ogre.getCounterCount(CounterType.DEVOTION)).isEqualTo(1);
    }

    @Test
    @DisplayName("With a Demon, target gets -X/-X equal to devotion counters")
    void debuffScalesWithDevotionCounters() {
        Permanent ogre = addReadyOgre();
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
        Permanent ogre = addReadyOgre();
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        forceMainPhase(player1);

        putDevotionCounters(ogre, 2);

        harness.activateAbility(player1, indexOf(player1, ogre), 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOff() {
        Permanent ogre = addReadyOgre();
        harness.addToBattlefield(player1, new DemonOfDeathsGate());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
        Permanent ogre = addReadyOgre();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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

    private Permanent addReadyOgre() {
        Permanent perm = new Permanent(new BloodthirstyOgre());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
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
