package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GolgariThug.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class GolgariThugTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it targets a creature card from its controller's graveyard and puts it on top of the library")
    void deathTriggerPutsTargetCreatureOnTopOfLibrary() {
        GolgariThug thug = new GolgariThug();
        Card creature = new GrizzlyBears();
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
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
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
    @DisplayName("May dredge Golgari Thug instead of drawing")
    void dredgesInsteadOfDrawing() {
        GolgariThug thug = new GolgariThug();
        List<Card> milled = List.of(new Forest(), new GrizzlyBears(), new Forest(), new Forest());
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
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new Forest(), new Forest()));

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
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears(), new Forest()));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(thug);
    }

    private void destroyThug() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
