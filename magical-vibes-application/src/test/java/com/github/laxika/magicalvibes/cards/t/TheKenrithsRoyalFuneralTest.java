package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsarethTheAwakener;
import com.github.laxika.magicalvibes.cards.i.InvokeTheDivine;
import com.github.laxika.magicalvibes.cards.v.VirtueOfKnowledge;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheKenrithsRoyalFuneral.class, EiganjoCastle.class, GrizzlyBears.class,
        IsarethTheAwakener.class, YomijiWhoBarsTheWay.class, InvokeTheDivine.class, VirtueOfKnowledge.class})
class TheKenrithsRoyalFuneralTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles up to two own legendary creature cards and uses the greatest mana value")
    void etbFiltersOwnLegendaryCreaturesAndDrawsAndLosesGreatestManaValue() {
        Card nonlegendaryCreature = new GrizzlyBears();
        Card legendaryLand = new EiganjoCastle();
        Card lowerManaValueCreature = new IsarethTheAwakener();
        Card higherManaValueCreature = new YomijiWhoBarsTheWay();
        Card opponentCreature = new IsarethTheAwakener();
        harness.setGraveyard(player1, List.of(
                nonlegendaryCreature, legendaryLand, lowerManaValueCreature, higherManaValueCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        castFuneral();
        harness.handleMultipleCardsChosen(player1,
                List.of(lowerManaValueCreature.getId(), higherManaValueCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(lowerManaValueCreature, higherManaValueCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(nonlegendaryCreature, legendaryLand);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Each exiled card reduces the generic cost of legendary spells only")
    void reducesLegendarySpellCostsByTrackedCardCount() {
        Card first = new IsarethTheAwakener();
        Card second = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(first, second));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new IsarethTheAwakener()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(harness.getGameActionAvailabilityService().getPlayableCardIndices(gd, player1.getId()))
                .as("step=%s active=%s passed=%s stack=%s spells=%s mana=%s", gd.currentStep,
                        gd.activePlayerId, gd.priorityPassedBy, gd.stack.size(),
                        gd.getSpellsCastThisTurnCount(player1.getId()),
                        gd.playerManaPools.get(player1.getId()).getTotal())
                .contains(0);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof IsarethTheAwakener);
    }

    @Test
    @DisplayName("Does not reduce nonlegendary spell costs")
    void doesNotReduceNonlegendarySpellCosts() {
        Card first = new IsarethTheAwakener();
        Card second = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(first, second));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void choosingNoTargetsDrawsNoCardsAndLosesNoLife() {
        Card creature = new IsarethTheAwakener();
        harness.setGraveyard(player1, List.of(creature));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void choosingOneCardDrawsAndLosesItsManaValue() {
        Card chosen = new IsarethTheAwakener();
        Card unchosen = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(chosen, unchosen));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void removingFuneralInResponseDoesNotPreventExileDrawOrLifeLoss() {
        Card creature = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(creature));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        var funeral = findPermanent(player1, "The Kenriths' Royal Funeral");
        harness.setHand(player2, List.of(new InvokeTheDivine()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, funeral.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "The Kenriths' Royal Funeral");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertLife(player1, 13);
    }

    @Test
    void additionalTriggersChooseTargetsIndependentlyAndUseOnlyTheirOwnExiledCards() {
        harness.addToBattlefield(player1, new VirtueOfKnowledge());
        Card lower = new IsarethTheAwakener();
        Card higher = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(lower, higher));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(lower.getId()));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(higher.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(lower, higher);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        harness.assertLife(player1, 10);
    }

    @Test
    void reductionCannotPayColoredManaCosts() {
        Card first = new IsarethTheAwakener();
        Card second = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(first, second));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new IsarethTheAwakener()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
    @Test
    void twoExiledCardsReduceGenericCostsByExactlyTwo() {
        Card first = new IsarethTheAwakener();
        Card second = new YomijiWhoBarsTheWay();
        harness.setGraveyard(player1, List.of(first, second));
        castFuneral();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new YomijiWhoBarsTheWay()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof YomijiWhoBarsTheWay);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
    private void castFuneral() {
        harness.setHand(player1, List.of(new TheKenrithsRoyalFuneral()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
