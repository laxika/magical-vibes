package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PlanTheHeist.class, GrizzlyBears.class})
class PlanTheHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils three before drawing three when the hand is empty")
    void surveilsThenDrawsWithEmptyHand() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        Card drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, graveyardCard, drawCard));
        harness.setHand(player1, List.of(new PlanTheHeist()));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard, graveyardCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(0, 1), List.of(2)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard, secondCard, drawCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    @DisplayName("Draws three without surveilling when the hand is not empty")
    void onlyDrawsWithNonEmptyHand() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Card thirdCard = new GrizzlyBears();
        Card spareCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.setHand(player1, List.of(new PlanTheHeist(), spareCard));
        addMana();

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(
                spareCard, firstCard, secondCard, thirdCard);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
