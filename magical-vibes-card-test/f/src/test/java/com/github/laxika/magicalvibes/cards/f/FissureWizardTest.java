package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FissureWizard.class})
class FissureWizardTest extends BaseCardTest {

    @Test
    @DisplayName("May discard a card to draw a card when it enters")
    void mayDiscardToDraw() {
        FissureWizard discarded = new FissureWizard();
        FissureWizard drawn = new FissureWizard();
        harness.setHand(player1, List.of(new FissureWizard(), discarded));
        harness.setLibrary(player1, List.of(drawn));

        castWizard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Fissure Wizard");
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("May decline to discard and draw when it enters")
    void mayDeclineToDiscard() {
        FissureWizard inHand = new FissureWizard();
        FissureWizard onTop = new FissureWizard();
        harness.setHand(player1, List.of(new FissureWizard(), inHand));
        harness.setLibrary(player1, List.of(onTop));

        castWizard();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot draw without a card to discard")
    void emptyHandDoesNotDraw() {
        FissureWizard onTop = new FissureWizard();
        harness.setHand(player1, List.of(new FissureWizard()));
        harness.setLibrary(player1, List.of(onTop));

        castWizard();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chooses one card to discard before drawing exactly one card")
    void choosesDiscardBeforeDrawing() {
        FissureWizard kept = new FissureWizard();
        FissureWizard discarded = new FissureWizard();
        FissureWizard drawn = new FissureWizard();
        FissureWizard next = new FissureWizard();
        harness.setHand(player1, List.of(new FissureWizard(), kept, discarded));
        harness.setLibrary(player1, List.of(drawn, next));

        castWizard();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn, next);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
    }

    private void castWizard() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
