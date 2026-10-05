package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        PatrolHound.class, DuskImp.class, Mountain.class
})
class PatrolHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        Permanent hound = addCreatureReady(player1, new PatrolHound());
        harness.setHand(player1, List.of(new DuskImp(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(0, 1);
        assertThat(hound.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Discarding a card grants first strike until end of turn")
    void discardingCardGrantsFirstStrikeUntilEndOfTurn() {
        Permanent hound = addCreatureReady(player1, new PatrolHound());
        harness.setHand(player1, List.of(new DuskImp()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dusk Imp");
        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A land can be discarded to pay the ability")
    void canDiscardAnyCard() {
        Permanent hound = addCreatureReady(player1, new PatrolHound());
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new PatrolHound());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard is paid before first strike is granted on resolution")
    void discardIsPaidBeforeResolution() {
        Permanent hound = addCreatureReady(player1, new PatrolHound());
        harness.setHand(player1, List.of(new Mountain(), new DuskImp()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInHand(player1, "Dusk Imp");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Patrol Hound can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new PatrolHound());
        hound.setSummoningSick(true);
        hound.setTapped(true);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.hasKeyword(gd, hound, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(hound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("First strike is granted only to the Patrol Hound that activated")
    void grantsFirstStrikeOnlyToSource() {
        Permanent source = addCreatureReady(player1, new PatrolHound());
        Permanent other = addCreatureReady(player1, new PatrolHound());
        Permanent opponent = addCreatureReady(player2, new PatrolHound());
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }
}
