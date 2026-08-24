package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PortentTracker.class, InvasionOfKamigawa.class, Mountain.class})
class PortentTrackerTest extends BaseCardTest {

    @Test
    void untapsTargetLand() {
        Permanent tracker = addTracker();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();

        harness.activateAbility(player1, indexOf(player1, tracker), 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    void cannotUntapNonlandPermanent() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player2, player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, tracker), 0, null, battle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removesDefenseCounterWhenOpponentProtectsBattle() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player2, player2.getId(), 3);

        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    @Test
    void putsDefenseCounterOnBattleProtectedByController() {
        Permanent tracker = addTracker();
        Permanent battle = addBattle(player2, player1.getId(), 1);

        harness.activateAbility(player1, indexOf(player1, tracker), 1, null, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
    }

    private Permanent addBattle(com.github.laxika.magicalvibes.model.Player controller,
                                java.util.UUID protectorId, int defenseCounters) {
        Permanent battle = harness.addToBattlefieldAndReturn(controller, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(protectorId);
        battle.setCounterCount(CounterType.DEFENSE, defenseCounters);
        return battle;
    }

    private Permanent addTracker() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new PortentTracker());
        tracker.setSummoningSick(false);
        return tracker;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
