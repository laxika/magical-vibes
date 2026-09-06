package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindlockOrb;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JestersCap.class, GrizzlyBears.class, MindlockOrb.class, Swamp.class})
class JestersCapTest extends BaseCardTest {

    private Permanent addCapReady() {
        Permanent cap = harness.addToBattlefieldAndReturn(player1, new JestersCap());
        cap.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
        return cap;
    }

    @Test
    @DisplayName("Exiles three chosen cards from target player's library and shuffles")
    void exilesThreeCards() {
        Card remainingCard = new Swamp();
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new Swamp(), new GrizzlyBears(), remainingCard));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Pick three cards (each pick re-presents the shrinking library from index 0)
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Three cards left the library, one remains
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        // Exiled cards are owned by the target player and, being an unrevealed search, face down
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("exiles a card face down. Library is shuffled."));
        // No further interaction pending
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        // Jester's Cap was sacrificed
        harness.assertNotOnBattlefield(player1, "Jester's Cap");
        harness.assertInGraveyard(player1, "Jester's Cap");
    }

    @Test
    @DisplayName("Exiles all cards when the library has fewer than three")
    void exilesFewerWhenLibrarySmall() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Swamp()));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot decline to find: three cards is a quantity, not a quality")
    void cannotDeclineToFind() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Swamp()));

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // CR 701.23d: a search for a bare quantity must find that many cards if they are there.
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }

    @Test
    @DisplayName("A prevented search shuffles only the searched library, not the searcher's own")
    void preventedSearchShufflesOnlyTheTargetsLibrary() {
        List<Card> ownLibrary = List.of(new GrizzlyBears(), new Swamp(), new GrizzlyBears(),
                new Swamp(), new GrizzlyBears(), new Swamp());
        harness.setLibrary(player1, ownLibrary);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Swamp()));

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
        harness.setLibrary(player2, List.of());

        addCapReady();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("searches " + gd.playerIdToName.get(player2.getId())
                        + "'s library but it is empty. Library is shuffled."));
        harness.assertInGraveyard(player1, "Jester's Cap");
    }

    @Test
    @DisplayName("Pays {2} before sacrificing itself to activate")
    void paysActivationCostBeforeSacrificing() {
        addCapReady();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Jester's Cap");
        harness.assertInGraveyard(player1, "Jester's Cap");
    }

    @Test
    @DisplayName("Can target its controller's library")
    void targetsControllersLibrary() {
        List<Card> opponentLibrary = List.of(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Swamp(), new GrizzlyBears()));
        harness.setLibrary(player2, opponentLibrary);

        addCapReady();
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
    }
}
