package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CephalidBroker.class, AvenFlock.class, Forest.class})
class CephalidBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent draws two cards, then discards two cards")
    void opponentDrawsThenDiscards() {
        Permanent broker = addCreatureReady(player1, new CephalidBroker());
        harness.setHand(player2, List.of(new AvenFlock(), new AvenFlock()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(broker.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target the controller and cannot target a permanent")
    void targetsPlayersOnly() {
        addCreatureReady(player1, new CephalidBroker());
        Permanent flock = addCreatureReady(player2, new AvenFlock());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flock.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new AvenFlock(), new AvenFlock()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }
}
