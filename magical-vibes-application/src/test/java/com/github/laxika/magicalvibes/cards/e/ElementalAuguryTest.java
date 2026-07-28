package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElementalAuguryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating enters a reorder of the top 3 cards of the target's library")
    void activatingEntersReorderOfTargetsLibrary() {
        harness.addToBattlefield(player1, new ElementalAugury());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card top0 = targetDeck.get(0);
        Card top1 = targetDeck.get(1);
        Card top2 = targetDeck.get(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Reordering places the chosen card on top of the target's library")
    void reorderingChangesTargetsTopCard() {
        harness.addToBattlefield(player1, new ElementalAugury());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card originallyThird = gd.playerDecks.get(player2.getId()).get(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(originallyThird);
    }

    @Test
    @DisplayName("Can target the controller's own library")
    void canTargetOwnLibrary() {
        harness.addToBattlefield(player1, new ElementalAugury());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card originallyThird = gd.playerDecks.get(player1.getId()).get(2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(originallyThird);
    }

    @Test
    @DisplayName("Library size is unchanged by the reorder")
    void librarySizeUnchanged() {
        harness.addToBattlefield(player1, new ElementalAugury());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int size = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 2, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(size);
    }
}
