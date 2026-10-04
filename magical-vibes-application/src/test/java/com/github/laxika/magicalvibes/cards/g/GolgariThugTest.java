package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariThug.class, Forest.class, GolgariBrownscale.class, Putrefy.class})
class GolgariThugTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it targets a creature card from its controller's graveyard and puts it on top of the library")
    void deathTriggerPutsTargetCreatureOnTopOfLibrary() {
        GolgariThug thug = new GolgariThug();
        Card creature = new GolgariBrownscale();
        Card nonCreature = new Forest();
        Card libraryCard = new Forest();
        harness.addToBattlefield(player1, thug);
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        destroyThug();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(thug.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(creature.getId(), libraryCard.getId());
        harness.assertNotInGraveyard(player1, "Golgari Brownscale");
        harness.assertInGraveyard(player1, "Golgari Thug");
    }

    @Test
    @DisplayName("Its death trigger can target Golgari Thug itself")
    void deathTriggerCanTargetItself() {
        GolgariThug thug = new GolgariThug();
        harness.addToBattlefield(player1, thug);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        destroyThug();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(thug.getId());

        harness.handleMultipleCardsChosen(player1, List.of(thug.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(thug.getId());
    }

    @Test
    @DisplayName("Its death trigger cannot target a creature card in an opponent's graveyard")
    void deathTriggerCannotTargetOpponentGraveyard() {
        GolgariThug thug = new GolgariThug();
        Card opponentCreature = new GolgariBrownscale();
        Card libraryCard = new Forest();
        harness.addToBattlefield(player1, thug);
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLibrary(player1, List.of(libraryCard));

        destroyThug();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(thug.getId());

        harness.handleMultipleCardsChosen(player1, List.of(thug.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(thug.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
    }

    @Test
    @DisplayName("May dredge Golgari Thug instead of drawing")
    void dredgesInsteadOfDrawing() {
        GolgariThug thug = new GolgariThug();
        List<Card> milled = List.of(new Forest(), new GolgariBrownscale(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(thug));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(thug);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        GolgariThug thug = new GolgariThug();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(thug));
        harness.setLibrary(player1, List.of(topCard, new GolgariBrownscale(), new Forest(), new Forest()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(thug);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot dredge when the library has fewer than four cards")
    void cannotDredgeWithTooFewLibraryCards() {
        GolgariThug thug = new GolgariThug();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(thug));
        harness.setLibrary(player1, List.of(topCard, new GolgariBrownscale(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(thug);
    }

    @Test
    @DisplayName("The death trigger does not move a target that has left the graveyard")
    void deathTriggerDoesNotMoveMissingTarget() {
        GolgariThug thug = new GolgariThug();
        Card creature = new GolgariBrownscale();
        Card libraryCard = new Forest();
        harness.addToBattlefield(player1, thug);
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));

        destroyThug();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(creature.getId())).toList());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Golgari Thug");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dredge mills only four cards and leaves the remaining library in order")
    void dredgeLeavesRemainingLibraryInOrder() {
        GolgariThug thug = new GolgariThug();
        Card firstRemaining = new GolgariBrownscale();
        Card lastRemaining = new Forest();
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(thug));
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), milled.get(3),
                firstRemaining, lastRemaining));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(thug).doesNotContain(firstRemaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstRemaining, lastRemaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Golgari Thug on the battlefield cannot replace a draw with dredge")
    void cannotDredgeFromBattlefield() {
        Card topCard = new Forest();
        harness.addToBattlefield(player1, new GolgariThug());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Golgari Thug");
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A player cannot dredge Golgari Thug from the opponent's graveyard")
    void cannotDredgeFromOpponentGraveyard() {
        GolgariThug thug = new GolgariThug();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(thug));
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).doesNotContain(thug);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(thug);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    private void destroyThug() {
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Golgari Thug"));
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
