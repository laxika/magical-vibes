package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstForMeaning.class, GrizzlyBears.class, OmenOfTheSea.class})
class ThirstForMeaningTest extends BaseCardTest {

    @Test
    void drawsThreeAndMayDiscardAnEnchantment() {
        castThirst(List.of(new GrizzlyBears(), new OmenOfTheSea(), new GrizzlyBears()),
                List.of(new ThirstForMeaning(), new GrizzlyBears()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        List<Card> hand = gd.playerHands.get(player1.getId());
        int enchantmentIndex = hand.indexOf(hand.stream()
                .filter(card -> card instanceof OmenOfTheSea)
                .findFirst()
                .orElseThrow());
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(enchantmentIndex);

        harness.handleCardChosen(player1, enchantmentIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void requiresTwoDiscardsWithoutAnEnchantment() {
        castThirst(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()),
                List.of(new ThirstForMeaning(), new GrizzlyBears()));

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private void castThirst(List<Card> library, List<Card> hand) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
