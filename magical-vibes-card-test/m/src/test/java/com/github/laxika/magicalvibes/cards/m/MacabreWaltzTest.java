package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.a.AzoriusHerald;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MacabreWaltz.class, AzoriusFirstWing.class, AzoriusHerald.class, AzoriusSignet.class})
class MacabreWaltzTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two target creature cards, then the controller discards a card")
    void returnsTwoCreaturesThenDiscards() {
        harness.setGraveyard(player1, List.of(new AzoriusFirstWing(), new AzoriusHerald()));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(validIds).hasSize(2);
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Both creatures returned, then one of them is discarded again.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2); // Macabre Waltz + discarded creature
        harness.assertInGraveyard(player1, "Macabre Waltz");
    }

    @Test
    @DisplayName("Only creature cards in the graveyard are legal targets")
    void onlyCreatureCardsAreLegalTargets() {
        Card creature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(creature, new AzoriusSignet()));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Returning one creature still forces the discard")
    void returningOneCreatureStillDiscards() {
        harness.setGraveyard(player1, List.of(new AzoriusFirstWing(), new AzoriusHerald()));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With an empty graveyard the spell resolves and nothing is discarded from an empty hand")
    void emptyGraveyardAndEmptyHand() {
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Macabre Waltz");
    }

    @Test
    @DisplayName("Discard still happens when no creature cards were returned")
    void discardHappensWithoutReturn() {
        harness.setGraveyard(player1, List.of(new AzoriusSignet()));
        harness.setHand(player1, List.of(new MacabreWaltz(), new AzoriusHerald()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Azorius Herald");
    }

    @Test
    @DisplayName("Choosing no creature cards still forces the discard")
    void choosingNoCreatureCardsStillDiscards() {
        AzoriusFirstWing creature = new AzoriusFirstWing();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MacabreWaltz(), new AzoriusHerald()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Azorius First-Wing");
        harness.assertInGraveyard(player1, "Azorius Herald");
        harness.assertInGraveyard(player1, "Macabre Waltz");
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void excludesCreatureCardsInOpponentGraveyard() {
        AzoriusFirstWing ownCreature = new AzoriusFirstWing();
        AzoriusHerald opponentCreature = new AzoriusHerald();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
    }

    @Test
    @DisplayName("If every chosen target leaves the graveyard, the spell does not discard")
    void allTargetsGonePreventsDiscard() {
        Card first = new AzoriusFirstWing();
        Card second = new AzoriusHerald();
        Card retained = new AzoriusSignet();
        harness.setGraveyard(player1, List.of(first, second));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setHand(player1, List.of(retained));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(first, second));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        harness.assertInGraveyard(player1, "Macabre Waltz");
    }

    @Test
    @DisplayName("With one target remaining, return it and discard a card already in hand")
    void oneTargetGoneStillReturnsAndDiscards() {
        Card removed = new AzoriusFirstWing();
        Card remaining = new AzoriusHerald();
        Card discarded = new AzoriusSignet();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setHand(player1, List.of(discarded));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded, remaining);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(removed, remaining);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Macabre Waltz");
    }
}
