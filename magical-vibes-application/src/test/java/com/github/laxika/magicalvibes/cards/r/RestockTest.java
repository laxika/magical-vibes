package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Restock.class, AlabasterLeech.class, Forest.class})
class RestockTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two chosen cards of any type from the graveyard to hand")
    void returnsTwoChosenCardsToHand() {
        harness.setGraveyard(player1, List.of(new AlabasterLeech(), new Forest()));
        harness.setHand(player1, List.of(new Restock()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        // Any card type is a legal target, not just creatures.
        assertThat(choice.validCardIds()).hasSize(2);

        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Alabaster Leech");
        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Alabaster Leech");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Restock exiles itself instead of going to the graveyard")
    void exilesItselfOnResolution() {
        harness.setGraveyard(player1, List.of(new AlabasterLeech(), new Forest()));
        harness.setHand(player1, List.of(new Restock()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Restock");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName())).contains("Restock");
    }

    @Test
    @DisplayName("Cannot cast with only one legal graveyard target")
    void cannotCastWithOnlyOneLegalTarget() {
        harness.setGraveyard(player1, List.of(new AlabasterLeech()));
        harness.setHand(player1, List.of(new Restock()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Restock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast with an empty graveyard")
    void cannotCastWithEmptyGraveyard() {
        harness.setHand(player1, List.of(new Restock()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Restock");
        assertThat(gd.stack).isEmpty();
    }
}
