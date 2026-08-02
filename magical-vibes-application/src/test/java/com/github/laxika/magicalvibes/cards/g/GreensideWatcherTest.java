package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreensideWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target Gate")
    void untapsTargetGate() {
        addCreatureReady(player1, new GreensideWatcher());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
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
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new RakdosGuildgate());
        gate.tap();

        harness.activateAbility(player1, 0, 0, null, gate.getId());
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Gate permanent")
    void cannotTargetNonGate() {
        addCreatureReady(player1, new GreensideWatcher());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }
}
