package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildestDreamsTest extends BaseCardTest {

    private void addManaForXTwo() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Returns exactly X target cards from your graveyard to your hand")
    void returnsExactlyXCardsFromOwnGraveyard() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new WildestDreams()));
        addManaForXTwo();

        harness.castSorcery(player1, 0, 2);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wildest Dreams is exiled after resolving")
    void isExiledAfterResolution() {
        WildestDreams dreams = new WildestDreams();
        harness.setHand(player1, List.of(dreams));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dreams);
        harness.assertNotInGraveyard(player1, "Wildest Dreams");
    }

    @Test
    @DisplayName("Must choose exactly X graveyard targets")
    void mustChooseExactlyXTargets() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new WildestDreams()));
        addManaForXTwo();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X=0 resolves without returning cards")
    void xZeroReturnsNoCards() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new WildestDreams()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
