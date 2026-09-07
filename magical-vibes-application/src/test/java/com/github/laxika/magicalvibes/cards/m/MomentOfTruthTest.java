package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomentOfTruth.class, GrizzlyBears.class})
class MomentOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into each destination")
    void distributesTopThreeCards() {
        Card topCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, handCard, bottomCard));
        harness.setHand(player1, List.of(new MomentOfTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandTopBottomChoice.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.HandTopBottom(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).contains(handCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomCard);
    }
}
