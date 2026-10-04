package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.n.NyxbornEidolon;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Forsaken Drifters")
@CardUsed({ForsakenDrifters.class, NyxbornEidolon.class})
class ForsakenDriftersTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, its controller mills four cards")
    void deathMillsFourCards() {
        harness.setLibrary(player1, List.of(new NyxbornEidolon(), new NyxbornEidolon(), new NyxbornEidolon(), new NyxbornEidolon()));
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof NyxbornEidolon).hasSize(4);
    }

    @Test
    @DisplayName("When it dies, it mills its controller's library rather than an opponent's")
    void deathMillsOnlyController() {
        harness.setLibrary(player1, List.of(new NyxbornEidolon(), new NyxbornEidolon(), new NyxbornEidolon(), new NyxbornEidolon()));
        int opponentDeckSize = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSize);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardSize);
    }

    @Test
    @DisplayName("When its controller has fewer than four cards, it mills the whole library")
    void deathMillsFewerCardsWhenLibraryIsSmall() {
        harness.setLibrary(player1, List.of(new NyxbornEidolon(), new NyxbornEidolon()));
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof NyxbornEidolon).hasSize(2);
    }

    @Test
    @DisplayName("Only the top four cards are milled from a larger library")
    void deathLeavesRemainingCardsInLibrary() {
        NyxbornEidolon remaining = new NyxbornEidolon();
        List<NyxbornEidolon> milled = List.of(new NyxbornEidolon(), new NyxbornEidolon(),
                new NyxbornEidolon(), new NyxbornEidolon());
        harness.setLibrary(player1, List.of(milled.get(0), milled.get(1), milled.get(2), milled.get(3), remaining));
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof NyxbornEidolon).containsExactlyElementsOf(milled);
    }

    @Test
    @DisplayName("The death trigger resolves with an empty library without causing a loss")
    void deathWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, new ForsakenDrifters());

        killForsakenDrifters();

        harness.assertInGraveyard(player1, "Forsaken Drifters");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An opponent's Forsaken Drifters mills that opponent's library")
    void opponentDeathMillsOpponentLibrary() {
        harness.setLibrary(player2, List.of(new NyxbornEidolon(), new NyxbornEidolon(),
                new NyxbornEidolon(), new NyxbornEidolon()));
        int ourDeckSize = gd.playerDecks.get(player1.getId()).size();
        harness.addToBattlefield(player2, new ForsakenDrifters());

        findPermanent(player2, "Forsaken Drifters").setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof NyxbornEidolon).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ourDeckSize);
    }

    private void killForsakenDrifters() {
        Permanent drifters = findPermanent(player1, "Forsaken Drifters");
        drifters.setMarkedDamage(4);
        harness.runStateBasedActions();
        resolveAllTriggers();
    }
}
