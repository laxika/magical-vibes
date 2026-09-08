package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VeiledCrocodileTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 4/4 Crocodile creature when a player has no cards in hand")
    void becomesCreatureWhenAPlayerHasNoCardsInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new VeiledCrocodile());
        Permanent crocodile = findPermanent(player1, "Veiled Crocodile");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crocodile)).isTrue();
        assertThat(gqs.isEnchantment(gd, crocodile)).isFalse();
        assertThat(crocodile.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(crocodile.getCard().getSubtypes()).containsExactly(CardSubtype.CROCODILE);
        assertThat(crocodile.getCard().getPower()).isEqualTo(4);
        assertThat(crocodile.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform while every player has a card in hand")
    void doesNotTransformWhileEveryPlayerHasCardsInHand() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new VeiledCrocodile());
        Permanent crocodile = findPermanent(player1, "Veiled Crocodile");

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crocodile)).isFalse();
        assertThat(gqs.isEnchantment(gd, crocodile)).isTrue();
    }
}
