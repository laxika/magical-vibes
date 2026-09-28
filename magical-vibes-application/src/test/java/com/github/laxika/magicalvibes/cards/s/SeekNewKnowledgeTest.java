package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeekNewKnowledge.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class SeekNewKnowledgeTest extends BaseCardTest {

    @Test
    void seeksTwoNonlandCardsThenPutsAHandCardOnBottom() {
        Card soughtCreature = new GrizzlyBears();
        Card soughtSpell = new Shock();
        Card cardToBottom = new Shock();
        Card forest = new Forest();
        Card island = new Island();
        harness.setHand(player1, List.of(new SeekNewKnowledge(), cardToBottom));
        harness.setLibrary(player1, List.of(forest, soughtCreature, island, soughtSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(cardToBottom, soughtCreature, soughtSpell);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, island);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> assertThat(choice.maxCount()).isEqualTo(1));

        harness.handleMultipleCardsChosen(player1, List.of(cardToBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(soughtCreature, soughtSpell);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(cardToBottom);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void seeksOnlyAvailableNonlandCards() {
        Card soughtCreature = new GrizzlyBears();
        Card cardToBottom = new Shock();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setHand(player1, List.of(new SeekNewKnowledge(), cardToBottom));
        harness.setLibrary(player1, List.of(forest, soughtCreature, island));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(cardToBottom, soughtCreature);
        harness.handleMultipleCardsChosen(player1, List.of(cardToBottom.getId()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(forest, island, cardToBottom);
        assertThat(gd.stack).isEmpty();
    }
}
