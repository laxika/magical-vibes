package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvanReveler.class, Forest.class})
class SilvanRevelerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws, discards, and returns a discarded land tapped")
    void returnsDiscardedLandTapped() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new SilvanReveler(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent returned = findPermanents(player1, "Forest").getFirst();
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not return a discarded nonland")
    void doesNotReturnDiscardedNonland() {
        SilvanReveler discarded = new SilvanReveler();
        harness.setLibrary(player1, List.of(discarded));
        harness.castFromHand(player1, new SilvanReveler(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(findPermanents(player1, "Silvan Reveler")).hasSize(1);
    }

    @Test
    @DisplayName("May pay to return itself from the graveyard when a land enters")
    void mayPayToReturnFromGraveyardOnLandfall() {
        SilvanReveler reveler = new SilvanReveler();
        harness.setGraveyard(player1, List.of(reveler));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Silvan Reveler");
        harness.assertNotInGraveyard(player1, "Silvan Reveler");
    }

    @Test
    @DisplayName("Declining the landfall payment keeps itself in the graveyard")
    void decliningLandfallPaymentKeepsItInGraveyard() {
        SilvanReveler reveler = new SilvanReveler();
        harness.setGraveyard(player1, List.of(reveler));
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Silvan Reveler");
    }

    @Test
    @DisplayName("An opponent's land does not trigger graveyard recursion")
    void opponentsLandDoesNotTriggerRecursion() {
        harness.setGraveyard(player1, List.of(new SilvanReveler()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Silvan Reveler");
    }

    @Test
    @DisplayName("An old landfall trigger cannot return a card that left and reentered the graveyard")
    void oldTriggerDoesNotReturnNewGraveyardObject() {
        SilvanReveler reveler = new SilvanReveler();
        harness.setGraveyard(player1, List.of(reveler));
        gd.markGraveyardEntry(reveler);
        prepareMainPhase();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.playLand(player1, 0);

        // Model the source leaving and returning before its pending trigger resolves.
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player1, List.of(reveler));
        gd.markGraveyardEntry(reveler);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Silvan Reveler");
        harness.assertNotInHand(player1, "Silvan Reveler");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
