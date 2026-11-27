package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.c.CabalPit;
import com.github.laxika.magicalvibes.cards.c.CephalidColiseum;
import com.github.laxika.magicalvibes.cards.f.FieldOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.o.OppositionAgent;
import com.github.laxika.magicalvibes.cards.p.PanglacialWurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Simplify;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CantSearchLibrariesEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NewFrontiers.class, CabalPit.class, CephalidColiseum.class, FieldOfTheDead.class,
        Forest.class, Island.class, ObNixilisUnshackled.class, Plains.class, Simplify.class,
        Swamp.class,
        AvenMindcensor.class,
        PsychogenicProbe.class,
        OppositionAgent.class,
        PanglacialWurm.class})
class NewFrontiersTest extends BaseCardTest {

    @Test
    @DisplayName("Each player may search for up to X basic lands, which enter tapped in APNAP order")
    void eachPlayerSearchesForUpToXBasicLandsTapped() {
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Plains firstPlains = new Plains();
        Plains secondPlains = new Plains();
        harness.setLibrary(player1, List.of(firstForest, secondForest, new Simplify()));
        harness.setLibrary(player2, List.of(firstPlains, secondPlains, new Simplify()));
        castNewFrontiers(2);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(firstForest, secondForest);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isFalse();

harness.handleMayAbilityChosen(player2, true);
        search = activeSearch();
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(firstPlains, secondPlains);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gameLogContains("shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Lands chosen by one search enter simultaneously")
    void chosenLandsEnterSimultaneously() {
        harness.addToBattlefield(player1, new FieldOfTheDead());
        harness.addToBattlefield(player1, new CabalPit());
        harness.addToBattlefield(player1, new CephalidColiseum());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setLibrary(player1, List.of(new Plains(), new Swamp()));
        harness.setLibrary(player2, List.of(new Forest()));
        castNewFrontiers(2);

harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(activeSearch()).isNull();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Zombie")).hasSize(2);
    }

    @Test
    @DisplayName("Each player may decline the search")
    void eachPlayerMayDecline() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(1);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(maySearch().playerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(maySearch()).isNull();
        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("shuffled.")).isFalse();
    }

    @Test
    @DisplayName("Each player may choose fewer than X basic lands")
    void eachPlayerMayChooseFewerThanX() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Simplify()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Simplify()));
        castNewFrontiers(2);

harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
    }

    @Test
    @DisplayName("A player without a basic land may decline to search")
    void playerWithoutBasicLandMayDecline() {
        harness.setLibrary(player1, List.of(new Simplify()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(1);

        assertThat(maySearch()).isNotNull();
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gameLogContains("shuffled.")).isFalse();
        assertThat(maySearch().playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
    }

    @Test
    @DisplayName("A player with an empty library may decline to search without triggering Ob Nixilis")
    void emptyLibraryMayDeclineSearch() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        castNewFrontiers(1);

        assertThat(maySearch()).isNotNull();
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With X=0 players still have the choice to search")
    void xZeroStillOffersOptionalSearch() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        castNewFrontiers(0);

        assertThat(maySearch()).isNotNull();
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(maySearch().playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(maySearch()).isNull();
        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Finding fewer lands than X completes both searches and resumes resolution")
    void fewerAvailableLandsThanXCompletesResolution() {
        harness.setLibrary(player1, List.of(new Forest(), new Simplify()));
        harness.setLibrary(player2, List.of(new Plains(), new Simplify()));
        castNewFrontiers(3);

harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A player may search and find nothing, and then shuffles; one who doesn't search doesn't")
    void searchingAndFindingNothingStillShuffles() {
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLibrary(player1, List.of(new Forest(), new Simplify()));
        harness.setLibrary(player2, List.of(new Plains(), new Simplify()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castNewFrontiers(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playersWhoSearchedLibraryThisTurn).containsExactly(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void castNewFrontiers(int xValue) {
        harness.setHand(player1, List.of(new NewFrontiers()));
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private PendingInteraction.MaySearchLibraryChoice maySearch() {
        return gd.interaction.activeInteraction(PendingInteraction.MaySearchLibraryChoice.class);
    }

    @Test
    @DisplayName("A searcher who declines with no basic land in the top four doesn't stop the next player's search")
    void restrictedSearchWithNoMatchContinuesToTheNextPlayer() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setLibrary(player1, List.of(
                new Simplify(), new Simplify(), new Simplify(), new Simplify(), new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));

        castNewFrontiers(1);
        // "Each player may search": the choice is player 1's, whatever lies in or below the top four.
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(maySearch().playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A player who can't search can't choose to, so doesn't shuffle")
    void playerWhoCantSearchDoesNotShuffle() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new CantSearchLibrariesEffect(Subjects.eachPlayer(), false), null, Duration.UNTIL_END_OF_TURN));

        castNewFrontiers(1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Under Aven Mindcensor a may-search looks the same whatever the library holds")
    void mindcensoredMaySearchDoesNotDependOnTheLibrary() {
        MaySearchOffer deeperMatch = offerUnderMindcensor(List.of(
                new Simplify(), new Simplify(), new Simplify(), new Simplify(), new Forest()));
        assertThat(deeperMatch.choices()).isEmpty();
        assertThat(deeperMatch.log()).noneMatch(line -> line.contains("finds no")
                || line.contains("Forest") || line.contains("Simplify"));
        // The opponent is told neither what is searchable nor what lies below it.
        assertThat(harness.getConn2().getSentMessages())
                .noneMatch(message -> message.contains("Forest") || message.contains("finds no"));

        MaySearchOffer noMatch = offerUnderMindcensor(List.of(
                new Simplify(), new Simplify(), new Simplify(), new Simplify(), new Simplify()));
        assertThat(noMatch).isEqualTo(deeperMatch);

        // A match among the top four changes only what the searcher is shown.
        MaySearchOffer topMatch = offerUnderMindcensor(List.of(
                new Forest(), new Simplify(), new Simplify(), new Simplify(), new Simplify()));
        assertThat(topMatch.choices()).containsExactly("Forest");
        assertThat(topMatch.log()).isEqualTo(deeperMatch.log());
        assertThat(topMatch.prompt()).isEqualTo(deeperMatch.prompt());
    }

    @Test
    @DisplayName("Declining a may-search under Aven Mindcensor neither shuffles nor searches")
    void decliningUnderMindcensorNeitherShufflesNorSearches() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        List<Card> library = List.of(new Simplify(), new Simplify(), new Simplify(), new Simplify(), new Forest());
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of(new Plains()));

        castNewFrontiers(1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(maySearch().playerId()).isEqualTo(player2.getId());
        assertThat(gd.playersWhoSearchedLibraryThisTurn).doesNotContain(player1.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("finds no"));
    }

    @Test
    @DisplayName("Casting from the library while searching is searching, and the next player still searches")
    void castingWhileSearchingContinuesTheQueue() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        PanglacialWurm wurm = new PanglacialWurm();
        harness.setLibrary(player1, List.of(wurm, new Simplify(), new Simplify(), new Simplify(), new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new NewFrontiers()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(activeSearch().params().cards()).containsExactly(wurm);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playersWhoSearchedLibraryThisTurn).contains(player1.getId());
harness.handleMayAbilityChosen(player2, true);
        assertThat(activeSearch().params().playerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A searcher whose find Opposition Agent exiles still shuffles")
    void searcherUnderOppositionAgentStillShuffles() {
        harness.addToBattlefield(player2, new OppositionAgent());
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Simplify()));
        harness.setLibrary(player2, List.of(new Plains()));

        castNewFrontiers(1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(activeSearch().decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Opposition Agent doesn't skip a may-search whose only match lies below the top four")
    void oppositionAgentIsOfferedTheEmptyMaySearch() {
        harness.addToBattlefield(player2, new OppositionAgent());
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setLibrary(player1, List.of(
                new Simplify(), new Simplify(), new Simplify(), new Simplify(), new Forest()));
        harness.setLibrary(player2, List.of(new Plains()));

        castNewFrontiers(1);
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(activeSearch().params().playerId()).isEqualTo(player1.getId());
        assertThat(activeSearch().decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(activeSearch().params().cards()).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("finds no"));
        harness.handleCardChosen(player2, -1);
        assertThat(maySearch().playerId()).isEqualTo(player2.getId());
    }

    private record MaySearchOffer(String prompt, List<String> choices, List<String> log) {
    }

    /** Casts New Frontiers in a game of its own and returns what its first searcher is offered and what all read. */
    private MaySearchOffer offerUnderMindcensor(List<Card> library) {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.addToBattlefield(player2, new AvenMindcensor());
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of(new Plains()));
        harness.clearMessages();
        int logStart = gd.gameLog.size();

        castNewFrontiers(1);
        assertThat(maySearch().playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch offer = activeSearch();
        assertThat(offer.params().playerId()).isEqualTo(player1.getId());
        return new MaySearchOffer(offer.messagePrompt(),
                offer.params().cards().stream().map(Card::getName).toList(),
                gd.gameLog.subList(logStart, gd.gameLog.size()).stream().map(GameLogEntry::plainText).toList());
    }
}
