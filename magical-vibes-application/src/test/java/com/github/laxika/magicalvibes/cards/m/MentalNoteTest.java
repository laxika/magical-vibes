package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MentalNote.class, HaplessResearcher.class})
class MentalNoteTest extends BaseCardTest {

    @Test
    void millsTwoCardsThenDraws() {
        Card milledCard1 = new HaplessResearcher();
        Card milledCard2 = new HaplessResearcher();
        Card drawnCard = new HaplessResearcher();

        harness.setLibrary(player1, List.of(milledCard1, milledCard2, drawnCard));
        harness.setHand(player1, List.of(new MentalNote()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(milledCard1, milledCard2)
                .anyMatch(card -> card.getName().equals("Mental Note"));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void millsAvailableCardsBeforeDrawingFromAnEmptyLibrary() {
        Card onlyLibraryCard = new HaplessResearcher();

        harness.setLibrary(player1, List.of(onlyLibraryCard));
        harness.setHand(player1, List.of(new MentalNote()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
