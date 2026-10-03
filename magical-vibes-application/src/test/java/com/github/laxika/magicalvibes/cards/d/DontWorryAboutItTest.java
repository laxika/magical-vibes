package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DontWorryAboutIt.class, Divination.class, GrizzlyBears.class, MindRot.class})
class DontWorryAboutItTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a card in its controller's hand")
    void entersAttachedToHandCard() {
        Card enchantedCard = new GrizzlyBears();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        harness.setHand(player1, List.of(auraCard, enchantedCard));
        addAuraMana();

        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Don't Worry About It");
        assertThat(aura.getAttachedTo()).isEqualTo(enchantedCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantedCard);
    }

    @Test
    @DisplayName("Reduces the enchanted card and copies it when cast")
    void reducesAndCopiesEnchantedCard() {
        Card enchantedCard = new Divination();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        harness.setHand(player1, List.of(auraCard, enchantedCard));
        addAuraMana();
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("The Aura goes to the graveyard when the enchanted card is discarded")
    void auraLeavesWhenEnchantedCardIsDiscarded() {
        Card enchantedCard = new GrizzlyBears();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        harness.setHand(player1, List.of(auraCard, enchantedCard, new MindRot()));
        addAuraMana();
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveSorcery(player1, 1, player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantedCard, auraCard);
        assertThat(countPermanents(player1, "Don't Worry About It")).isZero();
    }

    @Test
    @DisplayName("A copied creature spell resolves as a token and the original as a nontoken")
    void copiedCreatureEntersAsToken() {
        Card enchantedCard = new GrizzlyBears();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        harness.setHand(player1, List.of(auraCard, enchantedCard));
        addAuraMana();
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")
                .stream().filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(auraCard);
        assertThat(countPermanents(player1, "Don't Worry About It")).isZero();
    }

    @Test
    @DisplayName("The copied and original Divination each draw two cards")
    void bothSpellCopiesResolve() {
        Card enchantedCard = new Divination();
        DontWorryAboutIt auraCard = new DontWorryAboutIt();
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(auraCard, enchantedCard));
        addAuraMana();
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(auraCard, enchantedCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a card in an opponent's hand")
    void cannotEnchantOpponentsHandCard() {
        Card opposingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DontWorryAboutIt(), new GrizzlyBears()));
        harness.setHand(player2, List.of(opposingCard));
        addAuraMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opposingCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a card in your hand");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.stack).isEmpty();
    }

    private void addAuraMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
