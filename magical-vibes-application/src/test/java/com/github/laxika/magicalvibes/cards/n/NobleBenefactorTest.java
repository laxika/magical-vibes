package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NobleBenefactor.class, AetherFlash.class, MindStone.class, BenalishInfantry.class})
class NobleBenefactorTest extends BaseCardTest {

    private List<Card> setupLibrary(Player player) {
        Card noncreature = new MindStone();
        Card creature = new BenalishInfantry();
        List<Card> library = List.of(noncreature, creature);
        harness.setLibrary(player, library);
        return library;
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    /** Kills Noble Benefactor with Aether Flash while player2 is active. */
    private void killBenefactor() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.enterBattlefieldAndReturn(player2, new AetherFlash());
        harness.enterBattlefieldAndReturn(player1, new NobleBenefactor());
        harness.passBothPriorities(); // Aether Flash resolves -> Noble Benefactor dies
        harness.passBothPriorities(); // death trigger resolves
    }

    @Test
    @DisplayName("When Noble Benefactor dies each player searches in APNAP order and may take any card")
    void bothPlayersSearchForAnyCard() {
        List<Card> player1Library = setupLibrary(player1);
        List<Card> player2Library = setupLibrary(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        killBenefactor();

        // Active player (player2) is prompted first, and any card qualifies — not just creatures.
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(activeSearch().params().cards()).containsExactlyElementsOf(player2Library);
        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Library.get(0));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Library.get(0));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Library.get(1));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2Library.get(1));
    }

    @Test
    @DisplayName("A player may decline the search; the next player is still prompted")
    void playerMayDecline() {
        setupLibrary(player1);
        setupLibrary(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        killBenefactor();

        harness.handleCardChosen(player2, -1);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No player searches when library searches are prohibited")
    void searchIsSkippedWhenProhibited() {
        List<Card> player1Library = setupLibrary(player1);
        List<Card> player2Library = setupLibrary(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        gd.playersCantSearchLibrariesThisTurn = true;

        killBenefactor();

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(player1Library);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(player2Library);
    }

    @Test
    @DisplayName("A player with an empty library is skipped")
    void emptyLibraryPlayerIsSkipped() {
        List<Card> player1Library = setupLibrary(player1);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        killBenefactor();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Library.get(1));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nothing happens while Noble Benefactor stays on the battlefield")
    void noSearchWhileAlive() {
        harness.addToBattlefield(player1, new NobleBenefactor());
        harness.addToBattlefield(player2, new BenalishInfantry());
        setupLibrary(player1);
        setupLibrary(player2);

        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
    }
}
