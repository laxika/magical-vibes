package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GitaxianProbe.class})
class GitaxianProbeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Gitaxian Probe puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        Card probe = new GitaxianProbe();
        harness.setHand(player1, List.of(probe));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isSameAs(probe);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving Gitaxian Probe reveals opponent's hand in game log")
    void revealsOpponentHand() {
        Card cardInHand = new GitaxianProbe();
        harness.setHand(player2, List.of(cardInHand));

        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Resolving Gitaxian Probe against empty hand logs that hand is empty")
    void emptyHandLogged() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("Can target self to look at own hand")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at") && log.contains("hand"));
    }

    @Test
    @DisplayName("Resolving Gitaxian Probe draws a card")
    void drawsACard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Gitaxian Probe goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gitaxian Probe");
    }

    @Test
    @DisplayName("Can pay two life instead of blue mana and still draw from an empty-hand target")
    void paysLifeAndDrawsWithEmptyTargetHand() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.setHand(player2, List.of());
        Card drawnCard = new GitaxianProbe();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Gitaxian Probe");
    }

    @Test
    @DisplayName("Paying blue mana does not cost life even at one life")
    void paysBlueManaWithoutLosingLife() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gitaxian Probe");
    }

    @Test
    @DisplayName("Cannot pay the Phyrexian mana cost with only one life and no blue mana")
    void cannotPayLifeWithInsufficientLife() {
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new GitaxianProbe()));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertInHand(player1, "Gitaxian Probe");
        assertThat(gd.stack).isEmpty();
    }
}
