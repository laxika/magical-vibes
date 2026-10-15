package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenevolentBodyguard.class, HaplessResearcher.class, MentalNote.class})
class MentalNoteTest extends BaseCardTest {

    @Test
    void millsTwoCardsThenDraws() {
        Card milledCard1 = new BenevolentBodyguard();
        Card milledCard2 = new BenevolentBodyguard();
        Card drawnCard = new BenevolentBodyguard();
        Card spell = new MentalNote();

        harness.setLibrary(player1, List.of(milledCard1, milledCard2, drawnCard));
        harness.castFromHand(player1, spell, "{U}");

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(milledCard1, milledCard2)
                .contains(spell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void millsAvailableCardsBeforeDrawingFromAnEmptyLibrary() {
        Card onlyLibraryCard = new HaplessResearcher();

        harness.setLibrary(player1, List.of(onlyLibraryCard));
        harness.castFromHand(player1, new MentalNote(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void drawsFromEmptyLibraryAfterMillingNothing() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new MentalNote(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void losesAfterMillingExactlyTwoRemainingCards() {
        Card first = new BenevolentBodyguard();
        Card second = new HaplessResearcher();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new MentalNote(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void millsAndDrawsOnlyForItsController() {
        Card first = new BenevolentBodyguard();
        Card second = new HaplessResearcher();
        Card drawn = new BenevolentBodyguard();
        Card remaining = new HaplessResearcher();
        Card opponentsCard = new BenevolentBodyguard();
        harness.setLibrary(player1, List.of(opponentsCard));
        harness.setHand(player1, List.of());
        harness.setLibrary(player2, List.of(first, second, drawn, remaining));
        harness.castFromHand(player2, new MentalNote(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second).doesNotContain(drawn, remaining);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
