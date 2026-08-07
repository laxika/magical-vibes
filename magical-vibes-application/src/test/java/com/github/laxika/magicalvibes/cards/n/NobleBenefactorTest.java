package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NobleBenefactorTest extends BaseCardTest {

    private void setupLibrary(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new GrizzlyBears()));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    /** Kills the Noble Benefactor player1 controls with a Flame Javelin cast by the active player2. */
    private void killBenefactor() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        UUID benefactorId = harness.getPermanentId(player1, "Noble Benefactor");
        harness.castInstant(player2, 0, benefactorId);
        harness.passBothPriorities(); // Flame Javelin resolves -> Noble Benefactor dies
        harness.passBothPriorities(); // death trigger resolves
    }

    @Test
    @DisplayName("When Noble Benefactor dies each player searches in APNAP order and may take any card")
    void bothPlayersSearchForAnyCard() {
        harness.addToBattlefield(player1, new NobleBenefactor());
        setupLibrary(player1);
        setupLibrary(player2);
        harness.setHand(player1, List.of());

        killBenefactor();

        // Active player (player2) is prompted first, and any card qualifies — not just creatures.
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(activeSearch().params().cards()).hasSize(2);
        assertThat(activeSearch().params().remainingCount()).isEqualTo(1);

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Plains");
    }

    @Test
    @DisplayName("A player may decline the search; the next player is still prompted")
    void playerMayDecline() {
        harness.addToBattlefield(player1, new NobleBenefactor());
        setupLibrary(player1);
        setupLibrary(player2);
        harness.setHand(player1, List.of());

        killBenefactor();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A player with an empty library is skipped")
    void emptyLibraryPlayerIsSkipped() {
        harness.addToBattlefield(player1, new NobleBenefactor());
        setupLibrary(player1);
        gd.playerDecks.get(player2.getId()).clear();
        harness.setHand(player1, List.of());

        killBenefactor();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Nothing happens while Noble Benefactor stays on the battlefield")
    void noSearchWhileAlive() {
        harness.addToBattlefield(player1, new NobleBenefactor());
        harness.addToBattlefield(player2, new Forest());
        setupLibrary(player1);
        setupLibrary(player2);

        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
    }
}
