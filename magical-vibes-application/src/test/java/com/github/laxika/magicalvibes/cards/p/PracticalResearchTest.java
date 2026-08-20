package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PracticalResearchTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards and can discard one instant or sorcery")
    void drawsFourAndCanDiscardOneInstantOrSorcery() {
        castResearch(List.of(new Forest(), new Island(), new Mountain(), new Opt()),
                List.of(new PracticalResearch(), new GrizzlyBears(), new JalumTome()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        List<Card> hand = gd.playerHands.get(player1.getId());
        int optIndex = hand.indexOf(hand.stream().filter(card -> card instanceof Opt).findFirst().orElseThrow());
        PendingInteraction.DiscardChoice discard = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(optIndex);

        harness.handleCardChosen(player1, optIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can decline the one-card option and discard two cards")
    void canDiscardTwoCardsDespiteMatchingCard() {
        castResearch(List.of(new Forest(), new Island(), new Mountain(), new Opt()),
                List.of(new PracticalResearch(), new GrizzlyBears(), new JalumTome()));

        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.DiscardChoice discard = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.remainingCount()).isEqualTo(2);
        assertThat(discard.validIndices()).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Requires two discards when no instant or sorcery is available")
    void requiresTwoDiscardsWithoutMatchingCard() {
        castResearch(List.of(new Forest(), new Island(), new Mountain(), new Forest()),
                List.of(new PracticalResearch(), new GrizzlyBears(), new JalumTome()));

        PendingInteraction.DiscardChoice discard = gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    private void castResearch(List<Card> library, List<Card> hand) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
