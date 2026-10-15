package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.RumblingSentry;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstForMeaning.class, RumblingSentry.class, OmenOfTheSea.class, NyxbornCourser.class})
class ThirstForMeaningTest extends BaseCardTest {

    @Test
    void drawsThreeAndMayDiscardAnEnchantment() {
        castThirst(List.of(new RumblingSentry(), new OmenOfTheSea(), new RumblingSentry()),
                List.of(new ThirstForMeaning(), new RumblingSentry()));

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
        castThirst(List.of(new RumblingSentry(), new RumblingSentry(), new RumblingSentry()),
                List.of(new ThirstForMeaning(), new RumblingSentry()));

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayKeepTheEnchantmentAndDiscardTwoOtherCards() {
        OmenOfTheSea enchantment = new OmenOfTheSea();
        RumblingSentry first = new RumblingSentry();
        RumblingSentry second = new RumblingSentry();
        RumblingSentry kept = new RumblingSentry();
        castThirst(List.of(first, enchantment, second), List.of(new ThirstForMeaning(), kept));

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(first));
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(second));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(enchantment, kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosingTwoDiscardsStillRequiresTheSecondAfterDiscardingAnEnchantment() {
        OmenOfTheSea enchantment = new OmenOfTheSea();
        RumblingSentry otherDiscard = new RumblingSentry();
        castThirst(List.of(enchantment, otherDiscard, new RumblingSentry()),
                List.of(new ThirstForMeaning(), new RumblingSentry()));

        harness.handleMayAbilityChosen(player1, false);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(enchantment));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(otherDiscard));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment, otherDiscard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDiscardAnEnchantmentCreatureAlreadyInHand() {
        NyxbornCourser enchantmentCreature = new NyxbornCourser();
        RumblingSentry first = new RumblingSentry();
        RumblingSentry second = new RumblingSentry();
        RumblingSentry third = new RumblingSentry();
        castThirst(List.of(first, second, third), List.of(new ThirstForMeaning(), enchantmentCreature));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(enchantmentCreature));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantmentCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDiscardANonenchantmentForTheSingleCardChoice() {
        OmenOfTheSea enchantment = new OmenOfTheSea();
        RumblingSentry nonenchantment = new RumblingSentry();
        castThirst(List.of(enchantment, new RumblingSentry(), new RumblingSentry()),
                List.of(new ThirstForMeaning(), nonenchantment));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(nonenchantment));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(nonenchantment, enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonenchantment);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)
                .validIndices()).containsExactly(gd.playerHands.get(player1.getId()).indexOf(enchantment));

        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(enchantment));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(nonenchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castThirst(List<Card> library, List<Card> hand) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
    }
}
