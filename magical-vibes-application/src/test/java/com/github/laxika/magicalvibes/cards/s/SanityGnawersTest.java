package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SanityGnawersTest extends BaseCardTest {

    /** Casts Sanity Gnawers from player1's hand at {@code targetPlayerId} during player1's main phase. */
    private void castSanityGnawers(UUID targetPlayerId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, new ArrayList<>(List.of(new SanityGnawers())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB makes the targeted opponent discard a card at random")
    void etbTargetedOpponentDiscardsAtRandom() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GiantGrowth())));

        castSanityGnawers(player2.getId());
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB — random discard, no player choice

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("at random"));
    }

    @Test
    @DisplayName("Can target its own controller (any player is a legal target)")
    void canTargetController() {
        // player1's hand after casting Sanity Gnawers holds these two cards; one is discarded at random.
        harness.setHand(player1, new ArrayList<>(List.of(
                new SanityGnawers(), new GrizzlyBears(), new GiantGrowth())));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB — random discard

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB does nothing when the targeted player has an empty hand")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, new ArrayList<>());

        castSanityGnawers(player2.getId());
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
