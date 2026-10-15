package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralRecall.class})
class AncestralRecallTest extends BaseCardTest {

    @Test
    void controllerDrawsExactlyThreeCards() {
        AncestralRecall spell = new AncestralRecall();
        List<AncestralRecall> library = List.of(
                new AncestralRecall(), new AncestralRecall(), new AncestralRecall(), new AncestralRecall());
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, library);
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library.subList(0, 3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void opponentDrawsThreeWithoutLosingForAnExactlyThreeCardLibrary() {
        List<AncestralRecall> library = List.of(
                new AncestralRecall(), new AncestralRecall(), new AncestralRecall());
        harness.setHand(player1, List.of(new AncestralRecall()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void opponentLosesWhenUnableToDrawTheThirdCard() {
        List<AncestralRecall> library = List.of(new AncestralRecall(), new AncestralRecall());
        harness.setHand(player1, List.of(new AncestralRecall()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}
