package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.Thunderbolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GryffVanguard.class, Island.class, Thunderbolt.class})
class GryffVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new GryffVanguard()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.setLibrary(player1, List.of(new Island()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gryff Vanguard");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Drawing waits for the ETB trigger to resolve and draws only the top card")
    void drawWaitsForTriggerResolution() {
        Island topCard = new Island();
        Island nextCard = new Island();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new GryffVanguard()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gryff Vanguard");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("The entering creature's controller draws, including player two")
    void playerTwoDrawsFromOwnLibrary() {
        Island drawnCard = new Island();
        Island otherPlayersCard = new Island();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GryffVanguard()));
        harness.setLibrary(player1, List.of(otherPlayersCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherPlayersCard);
    }

    @Test
    @DisplayName("The ETB draw resolves after Gryff Vanguard is killed in response")
    void drawResolvesAfterSourceDies() {
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(new GryffVanguard()));
        harness.setHand(player2, List.of(new Thunderbolt()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castModalInstant(player2, 0, 1,
                List.of(harness.getPermanentId(player1, "Gryff Vanguard")));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gryff Vanguard");
        harness.assertInGraveyard(player1, "Gryff Vanguard");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }
}
