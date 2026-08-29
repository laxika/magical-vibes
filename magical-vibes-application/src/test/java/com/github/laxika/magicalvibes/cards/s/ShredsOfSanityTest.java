package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShredsOfSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one instant and one sorcery, then discards and exiles itself")
    void returnsInstantAndSorceryThenDiscardsAndExiles() {
        Card instant = new HolyDay();
        Card sorcery = new Divination();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Holy Day");
        harness.assertInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    @DisplayName("Does not allow two instant targets")
    void doesNotAllowTwoInstantTargets() {
        Card firstInstant = new HolyDay();
        Card secondInstant = new HolyDay();
        harness.setGraveyard(player1, List.of(firstInstant, secondInstant));
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstInstant.getId(), secondInstant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one instant");
    }

    @Test
    @DisplayName("Still discards and exiles when no graveyard target is chosen")
    void resolvesWithoutTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new Divination()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }
}
