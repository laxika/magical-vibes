package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeirdHarvestTest extends BaseCardTest {

    private void setupCreatureLibrary(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new SerraAngel(), new Plains()));
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Casting Weird Harvest puts it on the stack with the paid X")
    void castingPutsOnStackWithX() {
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Weird Harvest");
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("On resolution the active player is prompted first for up to X creature cards")
    void activePlayerPromptedFirst() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        // Only creature cards are offered (Plains is filtered out)
        assertThat(activeSearch().params().cards()).allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Each player may put up to X creatures into their hand, in APNAP order")
    void bothPlayersSearchForCreatures() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Active player (player1) takes two creatures
        gs.handleLibraryCardChosen(gd, player1, 0);
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Now the opponent (player2) is prompted
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        // player2 takes one creature, then declines the second
        gs.handleLibraryCardChosen(gd, player2, 0);
        gs.handleLibraryCardChosen(gd, player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).allMatch(c -> c.hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("A player may decline entirely; the next player still searches")
    void playerMayDeclineAndNextStillSearches() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Active player declines immediately
        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A player with no creatures in their library is skipped")
    void playerWithoutCreaturesIsSkipped() {
        List<Card> deck1 = gd.playerDecks.get(player1.getId());
        deck1.clear();
        deck1.addAll(List.of(new Plains(), new Forest()));
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // player1 has no creatures, so player2 is prompted directly
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("X=0 searches for nothing and resolves with no prompt")
    void xZeroResolvesWithoutSearch() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Weird Harvest"));
    }
}
