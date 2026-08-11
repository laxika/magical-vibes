package com.github.laxika.magicalvibes.cards.p;

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

class PharikasMenderTest extends BaseCardTest {

    private void castMender() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB prompts to return up to one creature or enchantment card")
    void etbPromptsForCreatureOrEnchantment() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment, new HolyDay()));
        harness.setHand(player1, List.of(new PharikasMender()));

        castMender();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
    }

    @Test
    @DisplayName("Returns the chosen creature or enchantment to hand")
    void returnsChosenCardToHand() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new PharikasMender()));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing zero targets leaves the graveyard unchanged")
    void choosingZeroTargetsReturnsNothing() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PharikasMender()));

        castMender();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Non-creature non-enchantment cards are not valid targets")
    void nonMatchingCardsAreNotTargets() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new PharikasMender()));

        castMender();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }
}
