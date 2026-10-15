package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecruiterOfTheGuard.class, Ornithopter.class, GrizzlyBears.class, GiantSpider.class,
        Forest.class, SparkElemental.class, Mortivore.class})
class RecruiterOfTheGuardTest extends BaseCardTest {

    @BeforeAll
    static void registerTestOracle() {
        Card.registerOracle("RecruiterOfTheGuard", new OracleData(
                "Recruiter of the Guard", CardType.CREATURE, java.util.Set.of(), "{2}{W}",
                CardColor.WHITE, List.of(CardColor.WHITE), List.of(CardColor.WHITE), java.util.Set.of(),
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                "When Recruiter of the Guard enters the battlefield, you may search your library for a creature card with toughness 2 or less, reveal it, put it into your hand, then shuffle.",
                1, 1, java.util.Set.of(), null, null, null));
    }

    @Test
    @DisplayName("ETB offers creature cards with toughness 2 or less")
    void etbOffersLowToughnessCreatures() {
        setupAndCast();
        List<Card> library = List.of(new Ornithopter(), new GrizzlyBears(), new GiantSpider());
        harness.setLibrary(player1, library);

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(library.get(0), library.get(1));
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand")
    void choosingCreaturePutsItIntoHand() {
        setupAndCast();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new GiantSpider()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningSearchLeavesLibraryUntouched() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GiantSpider()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Search excludes noncreatures and accepts power above two")
    void searchUsesCreatureTypeAndToughnessRatherThanPower() {
        setupAndCast();
        Card eligible = new SparkElemental();
        harness.setLibrary(player1, List.of(new Forest(), eligible, new GiantSpider()));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligible);
    }

    @Test
    @DisplayName("A restricted search may fail to find even with an eligible creature")
    void mayFailToFindEligibleCreature() {
        setupAndCast();
        List<Card> library = List.of(new GrizzlyBears(), new GiantSpider());
        harness.setLibrary(player1, library);

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("An accepted search with no eligible creature still shuffles")
    void noEligibleCreatureCompletesSearchAndShuffles() {
        setupAndCast();
        List<Card> library = List.of(new GiantSpider(), new Forest());
        harness.setLibrary(player1, library);

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library completes without choosing a card")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @Test
    @DisplayName("The chosen card is revealed, removed from the controller's library, and followed by a shuffle")
    void searchRevealsOneCardAndLeavesOpponentsLibraryUntouched() {
        setupAndCast();
        Card chosen = new GrizzlyBears();
        Card remaining = new Ornithopter();
        List<Card> opposingLibrary = List.of(new GrizzlyBears(), new GiantSpider());
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.setLibrary(player2, opposingLibrary);

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opposingLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        assertThat(gameLogContains("shuffled")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 3})
    @DisplayName("Characteristic-defining toughness is evaluated in the library, including opposing graveyards")
    void searchEvaluatesCharacteristicDefiningToughness(int creatureCount) {
        setupAndCast();
        Card mortivore = new Mortivore();
        Card fixedToughnessCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(mortivore, fixedToughnessCreature));
        List<Card> creatures = List.of(new GrizzlyBears(), new Ornithopter(), new GiantSpider());
        harness.setGraveyard(player1, creatures.subList(0, Math.min(creatureCount, 1)));
        harness.setGraveyard(player2, creatures.subList(Math.min(creatureCount, 1), creatureCount));

        resolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        if (creatureCount <= 2) {
            assertThat(search.params().cards()).containsExactlyInAnyOrder(mortivore, fixedToughnessCreature);
        } else {
            assertThat(search.params().cards()).containsExactly(fixedToughnessCreature);
        }
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new RecruiterOfTheGuard(), "{2}{W}");
    }

    private void resolveEtb() {
        resolveAllTriggers();
    }
}
