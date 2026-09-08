package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheThreeSeasons.class, Forest.class, Shock.class,
        SnowCoveredForest.class, SnowCoveredIsland.class})
class TheThreeSeasonsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills three cards")
    void chapterIMillsThreeCards() {
        Card first = new Forest();
        Card second = new Shock();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("Chapter II returns up to two snow permanents")
    void chapterIIReturnsUpToTwoSnowPermanents() {
        Card snowForest = new SnowCoveredForest();
        Card snowIsland = new SnowCoveredIsland();
        Card nonsnowForest = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(snowForest, nonsnowForest, snowIsland));
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(snowForest.getId(), snowIsland.getId());
        harness.handleMultipleCardsChosen(player1, List.of(snowForest.getId(), snowIsland.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(snowForest, snowIsland);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonsnowForest);
    }

    @Test
    @DisplayName("Chapter III chooses three cards from each graveyard, or all when fewer are available")
    void chapterIIIChoosesThreeFromEachGraveyard() {
        List<Card> ownCards = List.of(new Forest(), new Shock(), new Forest(), new Shock());
        List<Card> opponentCards = List.of(new Forest(), new Shock());
        harness.setGraveyard(player1, new ArrayList<>(ownCards));
        harness.setGraveyard(player2, new ArrayList<>(opponentCards));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent saga = addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(5);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(ownCards.get(0).getId(), ownCards.get(1).getId(),
                        opponentCards.get(0).getId(), opponentCards.get(1).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 5");

        harness.handleMultipleCardsChosen(player1, List.of(
                ownCards.get(0).getId(), ownCards.get(1).getId(), ownCards.get(2).getId(),
                opponentCards.get(0).getId(), opponentCards.get(1).getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCards.get(3), saga.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsAll(ownCards.subList(0, 3));
        assertThat(gd.playerDecks.get(player2.getId())).containsAll(opponentCards);
    }

    @Test
    @DisplayName("Chapter III enforces the three-card limit separately for each graveyard")
    void chapterIIILimitsEachGraveyardToThreeCards() {
        List<Card> ownCards = List.of(new Forest(), new Shock(), new Forest(), new Shock());
        List<Card> opponentCards = List.of(new Forest(), new Shock(), new Forest(), new Shock());
        harness.setGraveyard(player1, new ArrayList<>(ownCards));
        harness.setGraveyard(player2, new ArrayList<>(opponentCards));
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(ownCards.get(0).getId(), ownCards.get(1).getId(), ownCards.get(2).getId(),
                        ownCards.get(3).getId(), opponentCards.get(0).getId(),
                        opponentCards.get(1).getId(), opponentCards.get(2).getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most three");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheThreeSeasons());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
