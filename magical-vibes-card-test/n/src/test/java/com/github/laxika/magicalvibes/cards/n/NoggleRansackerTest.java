package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoggleRansacker.class})
class NoggleRansackerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes each player draw 2 then discard 1 at random")
    void etbDrawsTwoDiscardsOneForEachPlayer() {
        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new NoggleRansacker()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        // Resolve creature spell (ETB trigger goes on stack)
        harness.passBothPriorities();
        // Resolve ETB trigger
        harness.passBothPriorities();

        // Player 1: cast creature from hand, drew 2, discarded 1 at random = 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        // Player 2: started empty, drew 2, discarded 1 at random = 1 card in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        // Random discard should not prompt for input
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.assertOnBattlefield(player1, "Noggle Ransacker");
    }

    @Test
    @DisplayName("Discards all available when a player draws fewer than expected")
    void discardsAvailableWhenDeckRunsOut() {
        // Player 2 draws only 1 card, then discards it at random
        harness.setLibrary(player2, List.of(new NoggleRansacker()));

        harness.setHand(player1, List.of(new NoggleRansacker()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player 2 drew 1 (deck empty), discarded 1 at random = empty hand
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player discards exactly one card even with cards already in hand")
    void populatedHandsStillDiscardExactlyOneCard() {
        harness.setHand(player1, List.of(new NoggleRansacker(), new NoggleRansacker()));
        harness.setHand(player2, List.of(new NoggleRansacker(), new NoggleRansacker()));
        harness.setLibrary(player1, List.of(new NoggleRansacker(), new NoggleRansacker()));
        harness.setLibrary(player2, List.of(new NoggleRansacker(), new NoggleRansacker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
