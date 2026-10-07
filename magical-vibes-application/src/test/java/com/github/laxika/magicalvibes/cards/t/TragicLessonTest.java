package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TragicLesson.class, Forest.class, Plains.class})
class TragicLessonTest extends BaseCardTest {

    private void castTragicLesson() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new TragicLesson(), "{2}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws two, then returning a land avoids the discard")
    void returningALandAvoidsDiscard() {
        UUID landId = harness.addToBattlefieldAndReturn(player1, new Plains()).getId();

        castTragicLesson();

        // Drew two cards, then offered the "return a land instead of discarding" choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, landId);

        // The land is back in hand (2 drawn + returned land), the battlefield lost it, nothing discarded.
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Tragic Lesson");
    }

    @Test
    @DisplayName("Draws two, then declining the return forces a discard and keeps the land")
    void decliningForcesDiscard() {
        harness.addToBattlefield(player1, new Plains());

        castTragicLesson();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        // Declining prompts a discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // One of the two drawn cards was discarded; the land stayed on the battlefield.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("With no land to return, the discard is mandatory with no choice offered")
    void noLandForcesDiscardWithoutPrompt() {
        castTragicLesson();

        // No land means no "return a land" choice — the discard is imposed directly.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's land cannot be returned to avoid discarding")
    void opponentsLandDoesNotOfferReturn() {
        harness.addToBattlefield(player2, new Plains());

        castTragicLesson();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to that owner's hand")
    void borrowedLandReturnsToOwnerWithoutDiscard() {
        Plains land = new Plains();
        land.setOwnerId(player2.getId());
        UUID landId = harness.addToBattlefieldAndReturn(player1, land).getId();
        gd.stolenCreatures.put(landId, player2.getId());

        castTragicLesson();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, landId);

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Tragic Lesson");
    }
}
