package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MentalNote.class, BenevolentBodyguard.class})
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
}
