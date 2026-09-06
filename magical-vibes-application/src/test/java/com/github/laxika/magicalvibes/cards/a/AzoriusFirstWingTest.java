package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AzoriusFirstWing.class)
class AzoriusFirstWingTest extends BaseCardTest {

    @Test
    @DisplayName("Azorius First-Wing has protection from enchantments")
    void hasProtectionFromEnchantments() {
        harness.addToBattlefield(player1, new AzoriusFirstWing());
        Permanent firstWing = findPermanent(player1, "Azorius First-Wing");
        Permanent enchantment = new Permanent(createCard("Test Enchantment", CardType.ENCHANTMENT));
        Permanent creature = new Permanent(createCard("Test Creature", CardType.CREATURE));

        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, enchantment)).isTrue();
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, firstWing, creature)).isFalse();
    }

    private Card createCard(String name, CardType type) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(type);
        return card;
    }
}
