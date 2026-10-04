package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SimicGuildgate;
import com.github.laxika.magicalvibes.cards.s.Slaughterhorn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreensideWatcher.class, SimicGuildgate.class, Slaughterhorn.class})
class GreensideWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target Gate")
    void untapsTargetGate() {
        addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();

        harness.activateAbility(player1, 0, 0, null, gate.getId());
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Greenside Watcher").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can untap an opponent's Gate")
    void untapsOpponentGate() {
        addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new SimicGuildgate());
        gate.tap();

        harness.activateAbility(player1, 0, 0, null, gate.getId());
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Gate permanent")
    void cannotTargetNonGate() {
        addCreatureReady(player1, new GreensideWatcher());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new Slaughterhorn());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Gate");
    }

    @Test
    @DisplayName("Can target an untapped Gate")
    void canTargetUntappedGate() {
        Permanent watcher = addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());

        harness.activateAbility(player1, 0, 0, null, gate.getId());

        assertThat(watcher.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent watcher = harness.addToBattlefieldAndReturn(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, gate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(watcher.isTapped()).isFalse();
        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Watcher cannot activate its ability")
    void cannotActivateWhileTapped() {
        Permanent watcher = addCreatureReady(player1, new GreensideWatcher());
        watcher.tap();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, gate.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping waits for resolution and survives the source leaving")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent watcher = addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();

        harness.activateAbility(player1, 0, 0, null, gate.getId());

        assertThat(gate.isTapped()).isTrue();
        assertThat(watcher.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(watcher);
        gd.playerGraveyards.get(player1.getId()).add(watcher.getCard());
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An ability whose Gate target leaves does not untap another Gate")
    void doesNotRetargetWhenGateLeavesBattlefield() {
        Permanent watcher = addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        Permanent otherGate = harness.addToBattlefieldAndReturn(player1, new SimicGuildgate());
        gate.tap();
        otherGate.tap();

        harness.activateAbility(player1, 0, 0, null, gate.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gate);
        gd.playerGraveyards.get(player1.getId()).add(gate.getCard());
        harness.passBothPriorities();

        assertThat(otherGate.isTapped()).isTrue();
        assertThat(watcher.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
