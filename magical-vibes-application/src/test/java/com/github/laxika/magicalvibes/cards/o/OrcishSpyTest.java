package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrcishSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Looking at the top three cards leaves the library untouched and in order")
    void looksAtTopThreeWithoutChangingLibrary() {
        List<UUID> topBefore = setTopThreeCards(player2.getId());
        int sizeBefore = gd.playerDecks.get(player2.getId()).size();
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(sizeBefore);
        assertThat(deckAfter.stream().limit(3).map(Card::getId).toList()).isEqualTo(topBefore);
        // Non-blocking private reveal: no interaction is left pending and play proceeds.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top 3 cards"));
    }

    @Test
    @DisplayName("The looked-at cards' identities are never broadcast publicly")
    void doesNotLeakCardIdentities() {
        setTopThreeCards(player2.getId());
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Grizzly Bears"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Island"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("Forest"));
    }

    @Test
    @DisplayName("Activating the ability taps Orcish Spy")
    void tapsOnActivation() {
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(findPermanent(player1, "Orcish Spy").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An empty target library resolves with an empty-library log")
    void emptyLibraryResolvesGracefully() {
        gd.playerDecks.get(player2.getId()).clear();
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    private List<UUID> setTopThreeCards(UUID playerId) {
        List<Card> deck = gd.playerDecks.get(playerId);
        Card third = new Forest();
        Card second = new Island();
        Card first = new GrizzlyBears();
        deck.addFirst(third);
        deck.addFirst(second);
        deck.addFirst(first);
        return List.of(first.getId(), second.getId(), third.getId());
    }

    private void setupSpy() {
        harness.addToBattlefield(player1, new OrcishSpy());
        findPermanent(player1, "Orcish Spy").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
