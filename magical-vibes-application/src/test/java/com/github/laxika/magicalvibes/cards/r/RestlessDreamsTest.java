package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({RestlessDreams.class, GiantGrowth.class, GrizzlyBears.class, LlanowarElves.class})
class RestlessDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and returns exactly X target creature cards")
    void discardsAndReturnsExactlyXCreatures() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new RestlessDreams(), new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Restless Dreams");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Restless Dreams", "Giant Growth", "Giant Growth");
    }

    @Test
    @DisplayName("X=0 discards nothing and returns no cards")
    void xZeroDoesNothing() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RestlessDreams(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Giant Growth");
    }

    @Test
    @DisplayName("Only creature cards can be selected from the graveyard")
    void onlyCreatureCardsCanBeSelected() {
        Card bears = new GrizzlyBears();
        Card instant = new GiantGrowth();
        harness.setGraveyard(player1, List.of(bears, instant));
        harness.setHand(player1, List.of(new RestlessDreams(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Casting is rejected when fewer than X creature cards are available")
    void castRequiresXCreatureCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RestlessDreams(), new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching cards in graveyard");
    }
}
