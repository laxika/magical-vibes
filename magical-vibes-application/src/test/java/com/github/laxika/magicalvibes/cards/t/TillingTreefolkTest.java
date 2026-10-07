package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TillingTreefolk.class, Forest.class, Mountain.class, GrizzlyBears.class})
class TillingTreefolkTest extends BaseCardTest {

    private void castTreefolk() {
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB prompts to return up to two land cards from graveyard")
    void etbPromptsForLands() {
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);
    }

    @Test
    @DisplayName("Returning two lands puts both in hand")
    void returnsTwoLandsToHand() {
        harness.setGraveyard(player1, List.of(new Forest(), new Mountain()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Only land cards are valid targets")
    void onlyLandsAreValidTargets() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land, new GrizzlyBears()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(land.getId());
    }

    @Test
    @DisplayName("Choosing zero targets returns nothing")
    void choosingZeroReturnsNothing() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("No land cards in graveyard: enters with no prompt")
    void noLandsNoPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Tilling Treefolk");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Controller can decline the return at resolution after choosing targets")
    void canDeclineAfterChoosingTargets() {
        Card forest = new Forest();
        Card mountain = new Mountain();
        harness.setGraveyard(player1, List.of(forest, mountain));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId(), mountain.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing one land leaves the other land in the graveyard")
    void returnsOnlyChosenLand() {
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest, new Mountain()));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Opponent's lands cannot be chosen and the target cap remains two")
    void excludesOpponentLandsAndCapsTargets() {
        Card forest = new Forest();
        Card mountain = new Mountain();
        Card secondForest = new Forest();
        Card opponentLand = new Mountain();
        harness.setGraveyard(player1, List.of(forest, mountain, secondForest));
        harness.setGraveyard(player2, List.of(opponentLand));
        harness.setHand(player1, List.of(new TillingTreefolk()));

        castTreefolk();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                forest.getId(), mountain.getId(), secondForest.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
