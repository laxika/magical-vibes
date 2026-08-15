package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PullFromTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one instant and up to one sorcery from the graveyard")
    void returnsUpToOneInstantAndSorcery() {
        Card instant = new HolyDay();
        Card secondInstant = new HolyDay();
        Card sorcery = new LavaAxe();
        Card creature = new GrizzlyBears();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(instant, secondInstant, sorcery, creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                instant.getId(), secondInstant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(instant.getId(), sorcery.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(secondInstant.getId(), creature.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    @DisplayName("Does not allow two instant cards to be chosen")
    void doesNotAllowTwoInstantCards() {
        Card firstInstant = new HolyDay();
        Card secondInstant = new HolyDay();
        Card sorcery = new LavaAxe();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(firstInstant, secondInstant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstInstant.getId(), secondInstant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one instant card");

        harness.handleMultipleCardsChosen(player1, List.of(firstInstant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstInstant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("Exiles itself even when no eligible cards are in the graveyard")
    void exilesItselfWithNoEligibleCards() {
        Card creature = new GrizzlyBears();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }
}
