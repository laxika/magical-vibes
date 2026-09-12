package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AncientSilverback;
import com.github.laxika.magicalvibes.cards.a.AetherSting;
import com.github.laxika.magicalvibes.cards.c.Compost;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RofellossGift.class, AncientSilverback.class, AetherSting.class, Compost.class, HulkingOgre.class})
class RofellossGiftTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one enchantment for each green card revealed")
    void returnsEnchantmentForEachGreenCardRevealed() {
        Card greenCard = new AncientSilverback();
        Card nonGreenCard = new HulkingOgre();
        Card firstEnchantment = new Compost();
        Card secondEnchantment = new AetherSting();

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
        Card firstGreenCard = new AncientSilverback();
        Card secondGreenCard = new AncientSilverback();
        Card enchantment = new Compost();

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
        Card enchantment = new Compost();

        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new RofellossGift(), new HulkingOgre()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, enchantment.getName());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Returns one enchantment when only one of multiple green cards is revealed")
    void returnsForOnlyTheGreenCardsRevealed() {
        Card firstGreenCard = new AncientSilverback();
        Card secondGreenCard = new AncientSilverback();
        Card nonGreenCard = new HulkingOgre();
        Card enchantment = new Compost();
        Card secondEnchantment = new AetherSting();
        Card nonEnchantment = new HulkingOgre();

        harness.setGraveyard(player1, List.of(enchantment, secondEnchantment, nonEnchantment));
        harness.setHand(player1, List.of(new RofellossGift(), firstGreenCard, secondGreenCard, nonGreenCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.validCardIds()).containsExactly(firstGreenCard.getId(), secondGreenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstGreenCard.getId()));

        PendingInteraction.GraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(returnChoice).isNotNull();
        assertThat(returnChoice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, enchantment.getName());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondEnchantment, nonEnchantment);
        harness.assertInHand(player1, secondGreenCard.getName());
        harness.assertInHand(player1, nonGreenCard.getName());
    }

    @Test
    @DisplayName("Allows revealing zero green cards when green cards are available")
    void allowsRevealingZeroGreenCards() {
        Card greenCard = new AncientSilverback();
        Card enchantment = new Compost();

        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new RofellossGift(), greenCard));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.validCardIds()).containsExactly(greenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, enchantment.getName());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
