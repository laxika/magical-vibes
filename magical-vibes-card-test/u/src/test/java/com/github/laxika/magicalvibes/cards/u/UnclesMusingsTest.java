package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnclesMusings.class, Forest.class, GrizzlyBears.class, Shock.class})
class UnclesMusingsTest extends BaseCardTest {

    @Test
    void returnsUpToTheNumberOfColorsSpentAndExilesItself() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        Card shock = new Shock();
        UnclesMusings spell = new UnclesMusings();
        harness.setGraveyard(player1, List.of(bears, forest, shock));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(3);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void countsDistinctColorsAndMayReturnNoCards() {
        Card bears = new GrizzlyBears();
        UnclesMusings spell = new UnclesMusings();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    void returnsThreeCardsWhenThreeColorsWereSpent() {
        Card bears = new GrizzlyBears();
        Card firstForest = new Forest();
        Card secondForest = new Forest();
        Card remainingForest = new Forest();
        UnclesMusings spell = new UnclesMusings();
        harness.setGraveyard(player1, List.of(bears, firstForest, secondForest, remainingForest));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, firstForest, secondForest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remainingForest);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fourGreenManaAllowsOnlyOneReturn() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        UnclesMusings spell = new UnclesMusings();
        harness.setGraveyard(player1, List.of(bears, forest));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesItselfWhenNoPermanentCardsCanBeReturned() {
        Card shock = new Shock();
        UnclesMusings spell = new UnclesMusings();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }
}
