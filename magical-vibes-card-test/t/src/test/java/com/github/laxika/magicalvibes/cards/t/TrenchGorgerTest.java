package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrenchGorger.class, Plains.class, Forest.class, Panharmonicon.class})
class TrenchGorgerTest extends BaseCardTest {

    private Permanent castAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new TrenchGorger()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Trench Gorger");
    }

    private void chooseTwoLandsAndStop() {
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);
    }

    @Test
    @DisplayName("The optional ability exiles lands and sets base power and toughness to that count")
    void exilesLandsAndSetsBasePowerAndToughness() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest(), new Plains()));

        chooseTwoLandsAndStop();

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the ability leaves Trench Gorger unchanged")
    void abilityCanBeDeclined() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest()));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(6);
    }

    @Test
    @DisplayName("The base power and toughness remain locked if an exiled land leaves exile")
    void basePowerAndToughnessRemainLocked() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest(), new Plains()));

        chooseTwoLandsAndStop();
        UUID exiledId = gd.getCardsExiledByPermanent(gorger.getId()).getFirst().getId();
        gd.removeFromExile(exiledId);

        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting the search but choosing zero lands makes Trench Gorger die")
    void choosingZeroLandsSetsZeroToughness() {
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Trench Gorger");
        harness.assertInGraveyard(player1, "Trench Gorger");
    }

    @Test
    @DisplayName("Accepting a search of an empty library makes Trench Gorger die")
    void searchingEmptyLibrarySetsZeroToughness() {
        castAndResolve(List.of());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Trench Gorger");
        harness.assertInGraveyard(player1, "Trench Gorger");
    }

    @Test
    @DisplayName("Accepting a search with no land cards makes Trench Gorger die")
    void searchingLibraryWithoutLandsSetsZeroToughness() {
        TrenchGorger nonland = new TrenchGorger();
        castAndResolve(List.of(nonland));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        harness.assertNotOnBattlefield(player1, "Trench Gorger");
        harness.assertInGraveyard(player1, "Trench Gorger");
    }

    @Test
    @DisplayName("Only land cards are exiled and choosing every land finishes the search")
    void exilesAllLandsButLeavesNonlandsInLibrary() {
        TrenchGorger nonland = new TrenchGorger();
        Permanent gorger = castAndResolve(List.of(nonland, new Plains(), new Forest()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each additional trigger sets the size using only its own search")
    void additionalTriggerDoesNotCountLandsFromEarlierResolution() {
        harness.addToBattlefield(player1, new Panharmonicon());
        Permanent gorger = castAndResolve(List.of(new Plains(), new Forest(), new Plains()));

        chooseTwoLandsAndStop();
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(gorger.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, gorger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gorger)).isEqualTo(1);
    }
}
