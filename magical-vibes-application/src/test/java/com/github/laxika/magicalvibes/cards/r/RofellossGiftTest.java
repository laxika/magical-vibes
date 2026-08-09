package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WildGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RofellossGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one enchantment for each green card revealed")
    void returnsEnchantmentForEachGreenCardRevealed() {
        Card greenCard = new GrizzlyBears();
        Card nonGreenCard = new Shock();
        Card firstEnchantment = new Pacifism();
        Card secondEnchantment = new WildGrowth();

        harness.setGraveyard(player1, List.of(firstEnchantment, secondEnchantment));
        harness.setHand(player1, List.of(new RofellossGift(), greenCard, nonGreenCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(greenCard.getId()));

        PendingInteraction.GraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(returnChoice).isNotNull();
        assertThat(returnChoice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, firstEnchantment.getName());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondEnchantment);
        harness.assertInHand(player1, nonGreenCard.getName());
    }

    @Test
    @DisplayName("Returns all available enchantments when fewer exist than revealed cards")
    void returnsAllAvailableEnchantments() {
        Card firstGreenCard = new GrizzlyBears();
        Card secondGreenCard = new GrizzlyBears();
        Card enchantment = new Pacifism();

        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new RofellossGift(), firstGreenCard, secondGreenCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId(), secondGreenCard.getId()));

        harness.assertInHand(player1, enchantment.getName());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Returns nothing when no green cards are revealed")
    void returnsNothingWithoutGreenCards() {
        Card enchantment = new Pacifism();

        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new RofellossGift(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, enchantment.getName());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
