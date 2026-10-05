package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OliviasDragoon.class})
class OliviasDragoonTest extends BaseCardTest {

    @Test
    @DisplayName("Discard a card: this creature gains flying until end of turn")
    void discardGrantsFlying() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dragoon = harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        harness.setHand(player1, List.of(new OliviasDragoon()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Olivia's Dragoon");
        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dragoon = harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        harness.setHand(player1, List.of(new OliviasDragoon()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard is paid before the flying ability resolves")
    void discardIsPaidBeforeResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dragoon = harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        harness.setHand(player1, List.of(new OliviasDragoon()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Olivia's Dragoon");
        harness.assertNotInHand(player1, "Olivia's Dragoon");
        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(dragoon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick Dragoon can activate repeatedly on an opponent's turn")
    void canActivateRepeatedlyWhileTappedOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent dragoon = harness.addToBattlefieldAndReturn(player1, new OliviasDragoon());
        dragoon.setTapped(true);
        dragoon.setSummoningSick(true);
        harness.setHand(player1, List.of(new OliviasDragoon(), new OliviasDragoon()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Olivia's Dragoon");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, dragoon, Keyword.FLYING)).isTrue();
        assertThat(dragoon.isTapped()).isTrue();
    }
}
