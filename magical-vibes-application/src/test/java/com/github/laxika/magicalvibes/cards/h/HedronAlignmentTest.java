package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HedronAlignment.class)
class HedronAlignmentTest extends BaseCardTest {

    @Test
    @DisplayName("Wins after revealing a Hedron Alignment in all four zones")
    void winsWithOneInEachZone() {
        addFourAlignmentsWithFaceUpExile();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The upkeep choice is offered even when the condition is not met")
    void mayDeclineOrFailWithoutAllFourZones() {
        harness.addToBattlefield(player1, new HedronAlignment());
        harness.setHand(player1, List.of(new HedronAlignment()));
        harness.setGraveyard(player1, List.of(new HedronAlignment()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A face-down exiled card does not satisfy the alternate win condition")
    void faceDownExileDoesNotCount() {
        harness.addToBattlefield(player1, new HedronAlignment());
        harness.setHand(player1, List.of(new HedronAlignment()));
        harness.setGraveyard(player1, List.of(new HedronAlignment()));
        gd.addToExile(player1.getId(), new HedronAlignment(), null, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The activated ability scries one card")
    void scriesOneCard() {
        harness.addToBattlefield(player1, new HedronAlignment());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    private void addFourAlignmentsWithFaceUpExile() {
        harness.addToBattlefield(player1, new HedronAlignment());
        harness.setHand(player1, List.of(new HedronAlignment()));
        harness.setGraveyard(player1, List.of(new HedronAlignment()));
        harness.setExile(player1, List.of(new HedronAlignment()));
    }
}
