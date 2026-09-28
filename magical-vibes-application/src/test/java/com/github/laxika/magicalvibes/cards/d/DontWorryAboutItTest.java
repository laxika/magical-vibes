package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DontWorryAboutIt.class, Divination.class, GrizzlyBears.class})
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

    private void addAuraMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
