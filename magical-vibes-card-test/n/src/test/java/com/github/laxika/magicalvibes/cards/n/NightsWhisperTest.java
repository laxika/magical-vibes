package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightsWhisper.class})
class NightsWhisperTest extends BaseCardTest {

    @Test
    void drawsTwoCardsAndLosesTwoLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new NightsWhisper()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Night's Whisper");
    }

    @Test
    void losesTwoLifeEvenWhenLibraryHasOnlyOneCard() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new NightsWhisper()));

        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void losesLifeAndGameWhenLibraryIsEmpty() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void drawsBothCardsBeforeLethalLifeLoss() {
        harness.setLife(player1, 1);
        NightsWhisper first = new NightsWhisper();
        NightsWhisper second = new NightsWhisper();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new NightsWhisper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player1, -1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }
}
