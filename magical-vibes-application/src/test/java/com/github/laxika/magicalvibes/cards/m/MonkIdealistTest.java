package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonkIdealist.class, Pacifism.class, CoralMerfolk.class})
class MonkIdealistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted enchantment card from the graveyard to hand")
    void returnsEnchantmentFromGraveyardToHand() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        castMonkIdealist();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("ETB only allows enchantment cards to be targeted")
    void onlyEnchantmentCardsAreValidTargets() {
        CoralMerfolk merfolk = new CoralMerfolk();
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(merfolk, pacifism));

        castMonkIdealist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(pacifism.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(merfolk.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB returns only the one chosen enchantment card")
    void returnsOnlyChosenEnchantmentCard() {
        Pacifism chosenPacifism = new Pacifism();
        Pacifism remainingPacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(chosenPacifism, remainingPacifism));

        castMonkIdealist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                chosenPacifism.getId(), remainingPacifism.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosenPacifism.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosenPacifism.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(remainingPacifism.getId()));
    }

    @Test
    @DisplayName("ETB only targets enchantment cards in its controller's graveyard")
    void onlyTargetsItsControllersGraveyard() {
        Pacifism ownPacifism = new Pacifism();
        Pacifism opponentsPacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(ownPacifism));
        harness.setGraveyard(player2, List.of(opponentsPacifism));

        castMonkIdealist();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownPacifism.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownPacifism.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertInGraveyard(player2, "Pacifism");
    }

    @Test
    @DisplayName("ETB fizzles if the targeted enchantment leaves the graveyard")
    void fizzlesIfTargetLeavesGraveyard() {
        Pacifism pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        castMonkIdealist();
        harness.handleMultipleCardsChosen(player1, List.of(pacifism.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Pacifism");
    }

    @Test
    @DisplayName("ETB does not prompt when the graveyard has no enchantment cards")
    void noValidEnchantmentDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new CoralMerfolk()));

        castMonkIdealist();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Coral Merfolk");
    }

    private void castMonkIdealist() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MonkIdealist(), "{2}{W}");
        harness.passBothPriorities();
    }
}
