package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeirdHarvest.class, Forest.class, GrizzlyBears.class, Plains.class, SerraAngel.class,
        AvenMindcensor.class, ObNixilisUnshackled.class, PsychogenicProbe.class})
class WeirdHarvestTest extends BaseCardTest {

    private void setupCreatureLibrary(Player player) {
        harness.setLibrary(player, List.of(new GrizzlyBears(), new SerraAngel(), new Plains()));
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
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("On resolution the active player is prompted first for up to X creature cards")
    void activePlayerPromptedFirst() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

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

        harness.castAndResolveSorcery(player1, 0, 2);

        // Active player (player1) takes two creatures
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Now the opponent (player2) is prompted
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        // player2 takes one creature, then declines the second
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).allMatch(c -> c.hasType(CardType.CREATURE));
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A player may decline entirely; the next player still searches")
    void playerMayDeclineAndNextStillSearches() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        // Active player declines immediately
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A player with no creatures can decline the optional search")
    void playerWithoutCreaturesCanDeclineSearch() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Library-search restrictions skip every player's optional search")
    void searchesAreSkippedWhenProhibited() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);
        gd.playersCantSearchLibrariesThisTurn = true;

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Weird Harvest");
    }

    @Test
    @DisplayName("X=0 still lets each player decline the optional search")
    void xZeroStillOffersOptionalSearch() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Weird Harvest");
    }

    @Test
    @DisplayName("Searching triggers an opponent's search ability")
    void searchingTriggersOpponentSearchAbility() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, -1);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Players with empty libraries may decline without triggering shuffle abilities")
    void emptyLibrarySearchCanBeDeclined() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, -1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Taking two cards triggers an opponent's search ability once")
    void multiplePicksAreOneSearch() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, -1);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Stopping after taking one card still shuffles the searched library")
    void stoppingAfterFirstPickStillShuffles() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Later picks stay within the original top four and the next player still searches")
    void restrictedSearchRunningOutAdvancesToNextPlayer() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears fifth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, new Plains(), new Forest(), new Plains(), fifth));
        setupCreatureLibrary(player2);
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(activeSearch().params().cards()).containsExactly(first);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).contains(fifth);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, -1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Declining the optional search does not shuffle the library")
    void decliningSearchDoesNotShuffleLibrary() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(activeSearch()).isNotNull();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player2, -1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A prohibited optional search does not shuffle the library")
    void prohibitedSearchDoesNotShuffleLibrary() {
        setupCreatureLibrary(player1);
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        gd.playersCantSearchLibrariesThisTurn = true;

        harness.castAndResolveSorcery(player1, 0, 1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Aven Mindcensor limits the search to the top four cards")
    void avenMindcensorLimitsSearchToTopFourCards() {
        harness.setLibrary(player1, List.of(
                new Plains(), new Forest(), new Plains(), new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        assertThat(activeSearch().params().cards()).isEmpty();
        harness.handleCardChosen(player1, -1);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, -1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Creature choices stay hidden and out of hand until all players finish choosing")
    void choicesRemainHiddenUntilAllPlayersChoose() {
        setupCreatureLibrary(player1);
        setupCreatureLibrary(player2);
        harness.setHand(player1, List.of(new WeirdHarvest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        assertThat(gameLogContains("reveals")).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player2, 0);

        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
