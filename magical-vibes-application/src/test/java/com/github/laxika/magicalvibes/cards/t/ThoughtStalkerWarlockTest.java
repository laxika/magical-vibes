package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ThoughtStalkerWarlock.class, GrizzlyBears.class, Forest.class})
class ThoughtStalkerWarlockTest extends BaseCardTest {

    @Test
    @DisplayName("If the target opponent lost life, you choose a nonland card to discard")
    void targetLostLifeAllowsChoosingNonlandCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        castThoughtStalkerWarlock();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Forest");
    }

    @Test
    @DisplayName("If the target opponent did not lose life, they choose a card to discard")
    void targetDidNotLoseLifeDiscardsOneCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        castThoughtStalkerWarlock();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);

        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Grizzly Bears");
    }

    private void castThoughtStalkerWarlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ThoughtStalkerWarlock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
