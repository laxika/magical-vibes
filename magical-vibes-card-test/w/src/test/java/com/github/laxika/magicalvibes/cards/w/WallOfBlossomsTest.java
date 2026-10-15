package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfBlossoms.class, Forest.class})
class WallOfBlossomsTest extends BaseCardTest {

    @Test
    @DisplayName("When Wall of Blossoms enters, its controller draws a card")
    void etbDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new WallOfBlossoms(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("When Wall of Blossoms enters under an opponent's control, that player draws a card")
    void drawsForItsController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new WallOfBlossoms());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("The draw trigger resolves even after Wall of Blossoms leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        WallOfBlossoms topCard = new WallOfBlossoms();
        WallOfBlossoms remainingCard = new WallOfBlossoms();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        var wall = harness.enterBattlefieldAndReturn(player1, new WallOfBlossoms());

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wall));
        harness.assertInGraveyard(player1, "Wall of Blossoms");
        harness.assertNotOnBattlefield(player1, "Wall of Blossoms");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("The mandatory draw loses the game when the controller's library is empty")
    void emptyLibraryCausesLoss() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new WallOfBlossoms());

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
