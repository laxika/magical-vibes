package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExultantCultist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContingencyPlan.class, ExultantCultist.class})
class ContingencyPlanTest extends BaseCardTest {

    private GameData castAndResolve(Card... topCards) {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(topCards));
        harness.setHand(player1, List.of(new ContingencyPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
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

    @Test
    @DisplayName("Surveil 5 leaves cards below the top five untouched")
    void leavesDeeperCardsUntouched() {
        Card[] top = cards(7);
        GameData gd = castAndResolve(top);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(3, 1), List.of(4, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(top[3], top[1], top[5], top[6]);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(top[4], top[0], top[2])
                .doesNotContain(top[3], top[1], top[5], top[6]);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil 5 can put all five cards into the graveyard")
    void putsAllFiveCardsIntoGraveyard() {
        Card[] top = cards(5);
        GameData gd = castAndResolve(top);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(4, 3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil 5 resolves with an empty library without drawing a card")
    void resolvesWithEmptyLibrary() {
        GameData gd = castAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(ContingencyPlan.class::isInstance);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playersWhoSurveilledThisTurn).contains(player1.getId());
    }

    private Card[] cards(int count) {
        Card[] cards = new Card[count];
        for (int i = 0; i < count; i++) {
            cards[i] = new ExultantCultist();
        }
        return cards;
    }
}
