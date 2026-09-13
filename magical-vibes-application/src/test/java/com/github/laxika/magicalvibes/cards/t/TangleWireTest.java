package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.n.NobleStand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TangleWire.class, KorHaven.class, Mossdog.class, NobleStand.class})
class TangleWireTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four fade counters")
    void entersWithFourFadeCounters() {
        Permanent wire = harness.enterBattlefieldAndReturn(player1, new TangleWire());

        assertThat(wire.getCounterCount(CounterType.FADE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Taps the controller's matching permanents and removes a fade counter")
    void tapsMatchingPermanentsDuringOwnUpkeep() {
        Permanent wire = harness.enterBattlefieldAndReturn(player1, new TangleWire());
        Permanent mossdog = harness.enterBattlefieldAndReturn(player1, new Mossdog());
        Permanent haven = harness.enterBattlefieldAndReturn(player1, new KorHaven());
        Permanent stand = harness.enterBattlefieldAndReturn(player1, new NobleStand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(wire.getCounterCount(CounterType.FADE)).isEqualTo(3);
        assertThat(wire.isTapped()).isTrue();
        assertThat(mossdog.isTapped()).isTrue();
        assertThat(haven.isTapped()).isTrue();
        assertThat(stand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps matching permanents controlled by the active opponent")
    void tapsOnlyActiveOpponentsPermanents() {
        Permanent wire = harness.enterBattlefieldAndReturn(player1, new TangleWire());
        Permanent ownHaven = harness.enterBattlefieldAndReturn(player1, new KorHaven());
        Permanent opponentMossdog = harness.enterBattlefieldAndReturn(player2, new Mossdog());
        Permanent opponentHaven = harness.enterBattlefieldAndReturn(player2, new KorHaven());
        Permanent opponentStand = harness.enterBattlefieldAndReturn(player2, new NobleStand());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wire.getCounterCount(CounterType.FADE)).isEqualTo(4);
        assertThat(ownHaven.isTapped()).isFalse();
        assertThat(opponentMossdog.isTapped()).isTrue();
        assertThat(opponentHaven.isTapped()).isTrue();
        assertThat(opponentStand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires the affected player to choose exactly the required number")
    void choosesExactlyRequiredNumberWhenMoreAreAvailable() {
        Permanent wire = harness.enterBattlefieldAndReturn(player1, new TangleWire());
        wire.setCounterCount(CounterType.FADE, 1);
        Permanent firstMossdog = harness.enterBattlefieldAndReturn(player2, new Mossdog());
        Permanent secondMossdog = harness.enterBattlefieldAndReturn(player2, new Mossdog());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstMossdog.getId(), secondMossdog.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player2, List.of(firstMossdog.getId()));

        assertThat(firstMossdog.isTapped()).isTrue();
        assertThat(secondMossdog.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifices itself when its last fade counter is gone")
    void sacrificesWhenNoFadeCountersRemain() {
        Permanent wire = harness.enterBattlefieldAndReturn(player1, new TangleWire());
        wire.setCounterCount(CounterType.FADE, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wire);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(wire.getCard());
    }
}
