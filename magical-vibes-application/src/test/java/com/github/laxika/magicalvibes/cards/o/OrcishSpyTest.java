package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishSpy.class, Forest.class, Mountain.class, Plains.class})
class OrcishSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Looking at the top three cards leaves the library untouched and in order")
    void looksAtTopThreeWithoutChangingLibrary() {
        List<UUID> topBefore = setTopThreeCards(player2);
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
        setTopThreeCards(player2);
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anySatisfy(message -> assertThat(message)
                        .contains("Forest", "Mountain", "Plains"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gameLogContains("Forest")).isFalse();
        assertThat(gameLogContains("Mountain")).isFalse();
        assertThat(gameLogContains("Plains")).isFalse();
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
        Card first = new Forest();
        Card second = new Mountain();
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());

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
        harness.setLibrary(player2, List.of());
        setupSpy();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("library is empty")).isTrue();
    }

    private List<UUID> setTopThreeCards(Player player) {
        Card first = new Forest();
        Card second = new Mountain();
        Card third = new Plains();
        harness.setLibrary(player, List.of(first, second, third));
        return List.of(first.getId(), second.getId(), third.getId());
    }

    private void setupSpy() {
        addCreatureReady(player1, new OrcishSpy());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
