package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Briarhorn;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalCommand.class, Forest.class, Briarhorn.class, Lignify.class})
class PrimalCommandTest extends BaseCardTest {

    // Mode indices: 0 = target player gains 7 life, 1 = put target noncreature permanent on top of
    //               its owner's library, 2 = target player shuffles graveyard into library,
    //               3 = search library for a creature card to hand.

    @Test
    @DisplayName("Gain-life + search-creature: player gains 7 life and tutors a creature to hand")
    void gainLifeAndSearchCreature() {
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setLibrary(player1, List.of(new Forest(), new Briarhorn()));

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3}, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 27);

        // Restricted search only offers the creature card
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Briarhorn");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Briarhorn");
    }

    @Test
    @DisplayName("Bounce + shuffle-graveyard: noncreature permanent goes on top, graveyard shuffles into library")
    void bounceNoncreatureAndShuffleGraveyard() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setGraveyard(player2, List.of(new Briarhorn(), new Lignify()));
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 2},
                List.of(forestId, player2.getId()));
        harness.passBothPriorities();

        // Forest left the battlefield; graveyard emptied into the library
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // +1 (Forest to top) +2 (graveyard shuffled back)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore + 3);
    }

    @Test
    @DisplayName("Bounce mode targeting a creature is rejected")
    void bounceRejectsCreature() {
        harness.addToBattlefield(player2, new Briarhorn());
        UUID bearsId = harness.getPermanentId(player2, "Briarhorn");

        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainLifeAndPutNoncreatureOnTopUseSeparateTargets() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(player2.getId(), forestId));
        harness.passBothPriorities();

        harness.assertLife(player2, 27);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    void gainLifeAndShuffleCanTargetDifferentPlayers() {
        harness.setGraveyard(player1, List.of(new Briarhorn()));
        harness.setGraveyard(player2, List.of(new Lignify()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Lignify");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Briarhorn", "Primal Command");
    }

    @Test
    void gainLifeAndShuffleCanTargetTheSamePlayer() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Briarhorn()));
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(player1.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 27);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Briarhorn");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Primal Command");
    }

    @Test
    void shuffleBeforeSearchMakesGraveyardCreatureAvailable() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(new Briarhorn()));
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of(player1.getId()));
        harness.passBothPriorities();

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Briarhorn");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Briarhorn");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Primal Command");
    }

    @Test
    void putNoncreatureOnTopAndSearchWithoutFindingCreature() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setLibrary(player1, List.of(new Forest(), new Briarhorn()));
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of(forestId));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Briarhorn");
    }

    @Test
    void illegalOnlyTargetPreventsNontargetedSearch() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setLibrary(player1, List.of(new Briarhorn()));
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of(forestId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Briarhorn");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Primal Command");
    }

    @Test
    void legalPlayerTargetStillGainsLifeWhenPermanentTargetIsGone() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new PrimalCommand()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1},
                List.of(player1.getId(), forestId));
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 20);
    }
}
