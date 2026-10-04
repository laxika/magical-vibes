package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HavenOfTheSpiritDragon;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShelteredAerie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExplosiveVegetation.class, ShelteredAerie.class, Forest.class, Plains.class, HavenOfTheSpiritDragon.class})
class ExplosiveVegetationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two basic lands for the battlefield tapped")
    void offersBasicLandsForBattlefieldTapped() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        ShelteredAerie nonBasic = new ShelteredAerie();
        castWithLibrary(List.of(forest, plains, nonBasic));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, plains);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May find only one basic land")
    void mayFindOnlyOneBasicLand() {
        Forest forest = new Forest();
        ShelteredAerie nonBasic = new ShelteredAerie();
        castWithLibrary(List.of(forest, nonBasic));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find a basic land")
    void mayFailToFind() {
        castWithLibrary(List.of(new Forest(), new ShelteredAerie()));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves without a choice when the library has no basic lands")
    void resolvesWithoutBasicLand() {
        ShelteredAerie nonBasic = new ShelteredAerie();
        castWithLibrary(List.of(nonBasic));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonBasic);
    }

    @Test
    @DisplayName("May stop after one land even when another basic land is available")
    void mayStopAfterOneLandWithAnotherAvailable() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        castWithLibrary(List.of(forest, plains));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finds at most two lands and permits two with the same name")
    void findsAtMostTwoLandsWithTheSameName() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        castWithLibrary(List.of(first, second, third));

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2).allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Explosive Vegetation");
    }

    @Test
    @DisplayName("Does not offer nonbasic lands")
    void excludesNonbasicLands() {
        HavenOfTheSpiritDragon haven = new HavenOfTheSpiritDragon();
        Forest forest = new Forest();
        castWithLibrary(List.of(haven, forest));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(haven);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void resolvesWithEmptyLibrary() {
        castWithLibrary(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Explosive Vegetation");
    }

    private void castWithLibrary(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new ExplosiveVegetation(), "{3}{G}");
        harness.passBothPriorities();
    }
}
