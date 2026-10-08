package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SultaiSkullkeeper.class, Forest.class})
class SultaiSkullkeeperTest extends BaseCardTest {

    private void castAndResolveEtb() {
        harness.castFromHand(player1, new SultaiSkullkeeper(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB mills two cards from the controller's library")
    void etbMillsTwo() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB does not mill the opponent")
    void etbDoesNotMillOpponent() {
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardBefore);
    }

    @Test
    @DisplayName("ETB mills only the cards remaining in a smaller library")
    void etbMillsRemainderOfSmallLibrary() {
        harness.setLibrary(player1, List.of(new Forest()));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB moves the top two cards, leaving the third card in the library")
    void etbMillsTopTwoCards() {
        Forest first = new Forest();
        SultaiSkullkeeper second = new SultaiSkullkeeper();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("ETB resolves with an empty library without losing the game")
    void etbWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.assertOnBattlefield(player1, "Sultai Skullkeeper");
    }

    @Test
    @DisplayName("Cards are milled when the ETB trigger resolves, not when the creature resolves")
    void etbUsesTheStack() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new SultaiSkullkeeper(), "{1}{U}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sultai Skullkeeper");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }
}
