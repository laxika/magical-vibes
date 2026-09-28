package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimelineInquiry.class, GrizzlyBears.class})
class TimelineInquiryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and then discards one without teamwork")
    void drawsThreeThenDiscardsWithoutTeamwork() {
        Card discardedCard = new GrizzlyBears();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new TimelineInquiry(), discardedCard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("Teamwork draws three cards without discarding and taps the chosen creatures")
    void teamworkDrawsThreeWithoutDiscarding() {
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        Card thirdDraw = new GrizzlyBears();
        Card spell = new TimelineInquiry();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        Permanent teamworkCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teamworkCreature.getId()));
        harness.passBothPriorities();

        assertThat(teamworkCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
