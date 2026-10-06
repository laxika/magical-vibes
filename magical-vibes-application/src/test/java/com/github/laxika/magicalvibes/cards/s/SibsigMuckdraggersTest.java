package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EnhancedAwareness;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SibsigMuckdraggers.class, EnhancedAwareness.class})
class SibsigMuckdraggersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a chosen creature card from the graveyard to hand")
    void returnsCreatureToHand() {
        Card creature = new SibsigMuckdraggers();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Sibsig Muckdraggers");
        harness.assertNotInGraveyard(player1, "Sibsig Muckdraggers");
    }

    @Test
    @DisplayName("Only creature cards can be chosen")
    void onlyCreaturesCanBeChosen() {
        Card instant = new EnhancedAwareness();
        Card creature = new SibsigMuckdraggers();
        harness.setGraveyard(player1, List.of(instant, creature));
        castAndResolveEtb();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(creature);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters the battlefield without a graveyard choice when no creature is available")
    void noCreatureInGraveyard() {
        harness.setGraveyard(player1, List.of(new EnhancedAwareness()));
        castAndResolveEtb();

        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Sibsig Muckdraggers");
    }

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness(),
                new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new SibsigMuckdraggers()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6, 7));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player1, "Sibsig Muckdraggers");
    }

    @Test
    @DisplayName("The enter trigger requires a target and cannot be declined")
    void cannotDeclineCreatureReturn() {
        Card creature = new SibsigMuckdraggers();
        harness.setGraveyard(player1, List.of(creature));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Sibsig Muckdraggers");
    }

    @Test
    @DisplayName("Delve cannot pay the black mana requirement")
    void delveCannotPayColoredMana() {
        harness.setGraveyard(player1, List.of(
                new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness(),
                new EnhancedAwareness(), new EnhancedAwareness(), new EnhancedAwareness(),
                new EnhancedAwareness(), new EnhancedAwareness()));
        harness.setHand(player1, List.of(new SibsigMuckdraggers()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6, 7)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A removed graveyard target cannot be replaced with another creature")
    void removedTargetDoesNotReturnAnotherCreature() {
        Card target = new SibsigMuckdraggers();
        Card other = new SibsigMuckdraggers();
        harness.setGraveyard(player1, List.of(target, other));
        castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }
    private void castAndResolveEtb() {
        harness.castFromHand(player1, new SibsigMuckdraggers(), "{8}{B}");
        harness.passBothPriorities();
    }
}
