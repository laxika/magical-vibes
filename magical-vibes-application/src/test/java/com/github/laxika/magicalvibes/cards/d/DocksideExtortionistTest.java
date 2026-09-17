package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DocksideExtortionist.class)
class DocksideExtortionistTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure for each opposing artifact or enchantment")
    void createsTreasureForOpposingArtifactsAndEnchantments() {
        addPermanent(player1, CardType.ARTIFACT, "Own artifact");
        addPermanent(player2, CardType.ARTIFACT, "Opposing artifact");
        addPermanent(player2, CardType.ENCHANTMENT, "Opposing enchantment one");
        addPermanent(player2, CardType.ENCHANTMENT, "Opposing enchantment two");
        addPermanent(player2, CardType.CREATURE, "Opposing creature");

        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
    }

    @Test
    @DisplayName("Creates no Treasure when opponents control no artifacts or enchantments")
    void createsNoTreasureWithoutOpposingArtifactsOrEnchantments() {
        addPermanent(player2, CardType.CREATURE, "Opposing creature");

        harness.setHand(player1, List.of(new DocksideExtortionist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void addPermanent(com.github.laxika.magicalvibes.model.Player player,
                              CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        harness.addToBattlefield(player, card);
    }
}
