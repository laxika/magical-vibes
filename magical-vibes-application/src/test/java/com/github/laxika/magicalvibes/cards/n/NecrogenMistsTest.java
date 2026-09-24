package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecrogenMists.class, AlphaMyr.class, Ornithopter.class})
class NecrogenMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep makes that player discard a card")
    void eachPlayerDiscardsOnTheirUpkeep() {
        harness.addToBattlefield(player1, new NecrogenMists());
        harness.setHand(player1, List.of(new AlphaMyr()));
        harness.setHand(player2, List.of(new Ornithopter()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Alpha Myr");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("The active player chooses which card to discard")
    void activePlayerChoosesDiscard() {
        harness.addToBattlefield(player1, new NecrogenMists());
        harness.setHand(player1, List.of(new AlphaMyr(), new Ornithopter()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Alpha Myr");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Does nothing when the active player has no cards in hand")
    void emptyHandDiscardsNothing() {
        harness.addToBattlefield(player1, new NecrogenMists());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
