package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GolgariGuildgate;
import com.github.laxika.magicalvibes.cards.s.SteamVents;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircuitousRoute.class, Forest.class, GolgariGuildgate.class,
        CentaurPeacemaker.class, SteamVents.class})
class CircuitousRouteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers basic lands and Gates for the battlefield-tapped search")
    void resolvingOffersBasicLandsAndGates() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && (card.getSupertypes().contains(CardSupertype.BASIC)
                        || card.getSubtypes().contains(CardSubtype.GATE)))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Golgari Guildgate");
    }

    @Test
    @DisplayName("Chosen basic land and Gate enter the battlefield tapped")
    void chosenBasicLandAndGateEnterTapped() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        harness.assertInGraveyard(player1, "Circuitous Route");
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CircuitousRoute()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new GolgariGuildgate(), new CentaurPeacemaker()));
    }

    @Test
    void mayChooseNoCardsEvenWhenBothAreAvailable() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Circuitous Route");
    }

    @Test
    void mayStopAfterOneCardWithAnotherEligibleCardRemaining() {
        setupAndCast();
        setupLibrary();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Golgari Guildgate");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void mayChooseTwoGatesWithTheSameNameButNoThirdCard() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new GolgariGuildgate(), new GolgariGuildgate(), new Forest()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void finishesWhenOnlyOneEligibleCardExists() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new SteamVents(), new CentaurPeacemaker()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Steam Vents", "Centaur Peacemaker");
    }

    @Test
    void nonbasicLandWithBasicLandTypesIsNotEligible() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new SteamVents(), new CentaurPeacemaker()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Circuitous Route");
    }

    @Test
    void twoBasicLandsEnterTogetherAfterTheSearchChoices() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new CentaurPeacemaker()));
        harness.setLibrary(player2, List.of(new GolgariGuildgate()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Centaur Peacemaker");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Golgari Guildgate");
    }

    @Test
    void emptyLibraryDoesNotPreventResolution() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Circuitous Route");
    }
}
