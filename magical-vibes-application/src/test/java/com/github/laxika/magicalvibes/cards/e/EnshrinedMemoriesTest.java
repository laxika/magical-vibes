package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnshrinedMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals X cards, puts every creature into hand, and bottoms the rest")
    void putsAllRevealedCreaturesIntoHand() {
        Card firstCreature = new GrizzlyBears();
        Card firstNoncreature = new Shock();
        Card secondCreature = new LlanowarElves();
        Card secondNoncreature = new Plains();
        Card untouched = new Shock();
        setupTopCards(firstCreature, firstNoncreature, secondCreature, secondNoncreature, untouched);

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(firstNoncreature, secondNoncreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, secondNoncreature, firstNoncreature);
    }

    @Test
    @DisplayName("Only cards within X are revealed")
    void onlyLooksAtPaidXCards() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card outsideX = new LlanowarElves();
        setupTopCards(creature, noncreature, outsideX);

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Llanowar Elves"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(outsideX, noncreature);
    }

    private void setupTopCards(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
