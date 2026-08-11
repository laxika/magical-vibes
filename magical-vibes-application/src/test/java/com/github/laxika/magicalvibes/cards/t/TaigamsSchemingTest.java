package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaigamsSchemingTest extends BaseCardTest {

    private GameData castAndResolve(Card... topCards) {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(topCards));
        harness.setHand(player1, List.of(new TaigamsScheming()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        return gd;
    }

    @Test
    @DisplayName("Surveil 5 keeps all cards on top in the chosen order")
    void keepsAllCardsOnTop() {
        Card[] top = cards(5);
        GameData gd = castAndResolve(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(4, 3, 2, 1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .startsWith(top[4], top[3], top[2], top[1], top[0]);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top);
    }

    @Test
    @DisplayName("Surveil 5 puts selected cards into the graveyard")
    void putsSelectedCardsIntoGraveyard() {
        Card[] top = cards(5);
        GameData gd = castAndResolve(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 3), List.of(0, 2, 4)));

        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(top[1], top[3]);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(top[0], top[2], top[4])
                .doesNotContain(top[1], top[3]);
    }

    @Test
    @DisplayName("Surveil 5 uses all available cards when the library is short")
    void usesAllAvailableCards() {
        Card[] top = cards(3);
        GameData gd = castAndResolve(top);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
    }

    private Card[] cards(int count) {
        Card[] cards = new Card[count];
        for (int i = 0; i < count; i++) {
            cards[i] = new GrizzlyBears();
        }
        return cards;
    }
}
