package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Fervor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TangleWireTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the controller's matching permanents and removes a fade counter")
    void tapsMatchingPermanentsDuringOwnUpkeep() {
        Permanent wire = addReady(player1, new TangleWire());
        wire.setCounterCount(CounterType.FADE, 4);
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent fervor = addReady(player1, new Fervor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(wire.getCounterCount(CounterType.FADE)).isEqualTo(3);
        assertThat(wire.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(fervor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Taps matching permanents controlled by the active opponent")
    void tapsOnlyActiveOpponentsPermanents() {
        Permanent wire = addReady(player1, new TangleWire());
        wire.setCounterCount(CounterType.FADE, 4);
        Permanent ownForest = addReady(player1, new Forest());
        Permanent opponentBears = addReady(player2, new GrizzlyBears());
        Permanent opponentForest = addReady(player2, new Forest());
        Permanent opponentFervor = addReady(player2, new Fervor());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(wire.getCounterCount(CounterType.FADE)).isEqualTo(4);
        assertThat(ownForest.isTapped()).isFalse();
        assertThat(opponentBears.isTapped()).isTrue();
        assertThat(opponentForest.isTapped()).isTrue();
        assertThat(opponentFervor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires the affected player to choose exactly the required number")
    void choosesExactlyRequiredNumberWhenMoreAreAvailable() {
        Permanent wire = addReady(player1, new TangleWire());
        wire.setCounterCount(CounterType.FADE, 1);
        Permanent firstForest = addReady(player2, new Forest());
        Permanent secondForest = addReady(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstForest.getId(), secondForest.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();

        harness.handleMultiplePermanentsChosen(player2, List.of(firstForest.getId()));

        assertThat(firstForest.isTapped()).isTrue();
        assertThat(secondForest.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
