package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JestersCapTest extends BaseCardTest {

    private void addCapReady() {
        harness.addToBattlefield(player1, new JestersCap());
        Permanent cap = findPermanent(player1, "Jester's Cap");
        cap.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    @Test
    @DisplayName("Exiles three chosen cards from target player's library and shuffles")
    void exilesThreeCards() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card swamp = new Swamp();
        Card bears2 = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock, swamp, bears2));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Pick three cards (each pick re-presents the shrinking library from index 0)
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        // Three cards left the library, one remains
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        // Exiled cards are owned by the target player and, being an unrevealed search, face down
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        // No further interaction pending
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        // Jester's Cap was sacrificed
        harness.assertNotOnBattlefield(player1, "Jester's Cap");
        harness.assertInGraveyard(player1, "Jester's Cap");
    }

    @Test
    @DisplayName("Exiles all cards when the library has fewer than three")
    void exilesFewerWhenLibrarySmall() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(bears, shock));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot decline to find: three cards is a quantity, not a quality")
    void cannotDeclineToFind() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new Shock()));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // CR 701.23d: a search for a bare quantity must find that many cards if they are there.
        assertThatThrownBy(() -> gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A prevented search shuffles only the searched library, not the searcher's own")
    void preventedSearchShufflesOnlyTheTargetsLibrary() {
        List<Card> ownLibrary = List.of(new GrizzlyBears(), new Shock(), new Swamp(),
                new GrizzlyBears(), new Shock(), new Swamp());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(ownLibrary);
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new Shock()));

        addCapReady();
        harness.addToBattlefield(player1, new MindlockOrb());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // No search happens and nothing is exiled ...
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        // ... but "then that player shuffles" is a separate instruction that still resolves, and the
        // library it shuffles is the one that was to be searched.
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .contains(gd.playerIdToName.get(player2.getId()) + "'s library is shuffled.");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(ownLibrary);
    }

    @Test
    @DisplayName("Empty target library exiles nothing but still sacrifices the cap")
    void emptyLibrary() {
        gd.playerDecks.get(player2.getId()).clear();

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Jester's Cap");
    }
}
