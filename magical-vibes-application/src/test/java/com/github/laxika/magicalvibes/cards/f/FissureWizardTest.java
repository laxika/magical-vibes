package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FissureWizard.class, GrizzlyBears.class})
class FissureWizardTest extends BaseCardTest {

    @Test
    @DisplayName("May discard a card to draw a card when it enters")
    void mayDiscardToDraw() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new FissureWizard(), discarded));
        harness.setLibrary(player1, List.of(drawn));

        castWizard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("May decline to discard and draw when it enters")
    void mayDeclineToDiscard() {
        GrizzlyBears inHand = new GrizzlyBears();
        GrizzlyBears onTop = new GrizzlyBears();
        harness.setHand(player1, List.of(new FissureWizard(), inHand));
        harness.setLibrary(player1, List.of(onTop));

        castWizard();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castWizard() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
