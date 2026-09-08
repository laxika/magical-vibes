package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiredIdea.class, GrizzlyBears.class})
class InspiredIdeaTest extends BaseCardTest {

    private List<Card> library() {
        return new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Normal cast draws three cards and reduces the controller's maximum hand size")
    void normalCastDrawsAndReducesMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.setHand(player1, List.of(new InspiredIdea()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        gd.playerHands.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Cleave draws three cards without reducing the maximum hand size")
    void cleaveDrawsWithoutReducingMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, library());
        harness.setHand(player1, List.of(new InspiredIdea()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        gd.playerHands.get(player1.getId()).addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNull();
    }
}
