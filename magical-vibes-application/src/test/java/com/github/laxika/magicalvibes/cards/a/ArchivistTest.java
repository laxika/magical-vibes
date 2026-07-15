package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchivistTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack and taps Archivist")
    void activatingPutsOnStackAndTaps() {
        Permanent archivist = addReadyArchivist(player1);
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Archivist");
        assertThat(archivist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadyArchivist(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Drawing from empty deck is handled")
    void drawingFromEmptyDeck() {
        addReadyArchivist(player1);
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to draw"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent archivist = new Permanent(new Archivist());
        archivist.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(archivist);
        setDeck(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate twice in a turn because it requires tap")
    void cannotActivateTwice() {
        addReadyArchivist(player1);
        setDeck(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Helpers =====

    private Permanent addReadyArchivist(Player player) {
        Permanent perm = new Permanent(new Archivist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<? extends com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
