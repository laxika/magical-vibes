package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Note: the engine models a land's color as its color identity, so Moonring Island itself counts as
// one blue permanent toward its "two or more blue permanents" activation restriction.
class MoonringIslandTest extends BaseCardTest {

    @Test
    @DisplayName("Look ability begins a private look at the top card when controlling two or more blue permanents")
    void looksWithTwoBluePermanents() {
        Card topCard = setTopCard(player2.getId(), new Island());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Permanent island = addMoonring(player1);
        addBluePermanent(player1); // second blue permanent (Moonring counts as the first)
        harness.addMana(player1, ManaColor.BLUE, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, idx, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));

        // Closing the look leaves the library untouched, in order.
        gs.handleLibraryCardChosen(gd, player1, -1);
        List<Card> deckAfter = gd.playerDecks.get(player2.getId());
        assertThat(deckAfter).hasSize(deckSizeBefore);
        assertThat(deckAfter.getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Look ability cannot be activated with fewer than two blue permanents")
    void rejectedWithTooFewBluePermanents() {
        // Only Moonring Island itself (one blue permanent) — below the two-permanent threshold.
        Permanent island = addMoonring(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds blue mana")
    void manaAbilityAddsBlue() {
        Permanent island = addMoonring(player1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(island);
        harness.activateAbility(player1, idx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addMoonring(Player player) {
        harness.addToBattlefield(player, new MoonringIsland());
        Permanent island = findPermanent(player, "Moonring Island");
        island.setSummoningSick(false);
        island.untap();
        return island;
    }

    private void addBluePermanent(Player player) {
        Permanent p = new Permanent(new Island());
        gd.playerBattlefields.get(player.getId()).add(p);
    }

    private Card setTopCard(UUID playerId, Card card) {
        List<Card> deck = gd.playerDecks.get(playerId);
        deck.addFirst(card);
        return card;
    }
}
