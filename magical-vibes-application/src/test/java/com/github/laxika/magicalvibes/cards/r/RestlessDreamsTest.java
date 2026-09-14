package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Aquamoeba;
import com.github.laxika.magicalvibes.cards.b.BaskingRootwalla;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
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

@CardUsed({RestlessDreams.class, Aquamoeba.class, BaskingRootwalla.class, DeepAnalysis.class})
class RestlessDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Discards X cards and returns exactly X target creature cards")
    void discardsAndReturnsExactlyXCreatures() {
        Card rootwalla = new BaskingRootwalla();
        Card aquamoeba = new Aquamoeba();
        harness.setGraveyard(player1, List.of(rootwalla, aquamoeba));
        harness.setHand(player1, List.of(new RestlessDreams(), new DeepAnalysis(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2));
        harness.handleMultipleCardsChosen(player1, List.of(rootwalla.getId(), aquamoeba.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Basking Rootwalla");
        harness.assertInHand(player1, "Aquamoeba");
        harness.assertInGraveyard(player1, "Restless Dreams");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Restless Dreams", "Deep Analysis", "Deep Analysis");
    }

    @Test
    @DisplayName("X=0 discards nothing and returns no cards")
    void xZeroDoesNothing() {
        Card rootwalla = new BaskingRootwalla();
        harness.setGraveyard(player1, List.of(rootwalla));
        harness.setHand(player1, List.of(new RestlessDreams(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 0, List.of(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rootwalla);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Deep Analysis");
    }

    @Test
    @DisplayName("Only creature cards can be selected from the graveyard")
    void onlyCreatureCardsCanBeSelected() {
        Card rootwalla = new BaskingRootwalla();
        Card sorcery = new DeepAnalysis();
        harness.setGraveyard(player1, List.of(rootwalla, sorcery));
        harness.setHand(player1, List.of(new RestlessDreams(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(rootwalla.getId());
    }

    @Test
    @DisplayName("Only creature cards in your graveyard can be selected")
    void onlyYourGraveyardCanBeSelected() {
        Card ownCreature = new BaskingRootwalla();
        Card opponentCreature = new Aquamoeba();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new RestlessDreams(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
    }

    @Test
    @DisplayName("A creature discarded as an additional cost is not a target")
    void discardedCreatureIsNotTargetable() {
        Card graveyardCreature = new BaskingRootwalla();
        Card discardedCreature = new Aquamoeba();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new RestlessDreams(), discardedCreature));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithDiscards(player1, 0, 1, List.of(), List.of(1));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(graveyardCreature.getId());
    }

    @Test
    @DisplayName("Casting is rejected when fewer than X creature cards are available")
    void castRequiresXCreatureCards() {
        harness.setGraveyard(player1, List.of(new BaskingRootwalla()));
        harness.setHand(player1, List.of(new RestlessDreams(), new DeepAnalysis(), new DeepAnalysis()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() ->
                harness.castSorceryWithDiscards(player1, 0, 2, List.of(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching cards in graveyard");
    }
}
