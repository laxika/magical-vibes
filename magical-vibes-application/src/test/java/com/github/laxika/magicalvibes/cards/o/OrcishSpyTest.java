package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.i.IcatianStore;
import com.github.laxika.magicalvibes.cards.r.RainbowVale;
import com.github.laxika.magicalvibes.cards.r.RuinsOfTrokair;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishSpy.class, IcatianStore.class, RainbowVale.class, RuinsOfTrokair.class})
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
        assertThat(gameLogContains("looks at the top 3 cards")).isTrue();
    }

    @Test
    @DisplayName("The looked-at cards' identities are never broadcast publicly")
    void doesNotLeakCardIdentities() {
        setTopThreeCards(player2.getId());
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Rainbow Vale", "Icatian Store", "Ruins of Trokair"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gameLogContains("Rainbow Vale")).isFalse();
        assertThat(gameLogContains("Icatian Store")).isFalse();
        assertThat(gameLogContains("Ruins of Trokair")).isFalse();
    }

    @Test
    @DisplayName("Activating the ability taps Orcish Spy")
    void tapsOnActivation() {
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(findPermanent(player1, "Orcish Spy").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target the controller's short library")
    void looksAtAvailableCardsInOwnLibrary() {
        Card first = new RainbowVale();
        Card second = new IcatianStore();
        harness.setLibrary(player1, List.of(first, second));
        setupSpy();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gameLogContains("looks at the top 2 cards")).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void rejectsPermanentTarget() {
        setupSpy();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RainbowVale());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("The ability requires a player target")
    void requiresPlayerTarget() {
        setupSpy();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    @Test
    @DisplayName("An empty target library resolves with an empty-library log")
    void emptyLibraryResolvesGracefully() {
        gd.playerDecks.get(player2.getId()).clear();
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("library is empty")).isTrue();
    }

    private List<UUID> setTopThreeCards(UUID playerId) {
        List<Card> deck = gd.playerDecks.get(playerId);
        Card third = new RuinsOfTrokair();
        Card second = new IcatianStore();
        Card first = new RainbowVale();
        deck.addFirst(third);
        deck.addFirst(second);
        deck.addFirst(first);
        return List.of(first.getId(), second.getId(), third.getId());
    }

    private void setupSpy() {
        addCreatureReady(player1, new OrcishSpy());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
