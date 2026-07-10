package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrimalCommandTest extends BaseCardTest {

    // Mode indices: 0 = target player gains 7 life, 1 = put target noncreature permanent on top of
    //               its owner's library, 2 = target player shuffles graveyard into library,
    //               3 = search library for a creature card to hand.

    @Test
    @DisplayName("Gain-life + search-creature: player gains 7 life and tutors a creature to hand")
    void gainLifeAndSearchCreature() {
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new GrizzlyBears()));

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3}, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(27);

        // Restricted search only offers the creature card
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Grizzly Bears");
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Bounce + shuffle-graveyard: noncreature permanent goes on top, graveyard shuffles into library")
    void bounceNoncreatureAndShuffleGraveyard() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LightningBolt()));
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(forestId, player2.getId()));
        harness.passBothPriorities();

        // Forest left the battlefield; graveyard emptied into the library
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // +1 (Forest to top) +2 (graveyard shuffled back)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore + 3);
    }

    @Test
    @DisplayName("Bounce mode targeting a creature is rejected")
    void bounceRejectsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
