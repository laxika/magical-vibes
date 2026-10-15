package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecretRendezvous.class, Forest.class, AlmsCollector.class})
class SecretRendezvousTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the caster and target opponent each draw three cards")
    void eachDrawsThreeCards() {
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Secret Rendezvous");
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Requires an opponent target to cast")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Secret Rendezvous");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both players draw when the second player is the caster")
    void secondPlayerCanTargetFirstPlayer() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SecretRendezvous()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Secret Rendezvous");
    }

    @Test
    @DisplayName("Caster's Alms Collector replaces the opponent's three-card draw")
    void casterCollectorReplacesOpponentDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Opponent's Alms Collector replaces the caster's three-card draw")
    void opponentCollectorReplacesCasterDraw() {
        harness.addToBattlefield(player2, new AlmsCollector());
        harness.setHand(player1, List.of(new SecretRendezvous()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
