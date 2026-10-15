package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderManBrooklynVisionary.class, Forest.class, GrizzlyBears.class})
class SpiderManBrooklynVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield searches for a basic land and puts it tapped")
    void searchesForBasicLandTapped() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new SpiderManBrooklynVisionary(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent searchedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(searchedForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can be cast with web-slinging by returning a tapped creature")
    void castsWithWebSlinging() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man, Brooklyn Visionary");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Web-slinging requires a tapped creature")
    void requiresTappedCreature() {
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(untappedCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Web-slinging returns the creature while paying costs, before the spell resolves")
    void returnsCreatureBeforeResolution() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Spider-Man, Brooklyn Visionary");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Web-slinging cannot return an opponent's tapped creature")
    void cannotReturnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Spider-Man, Brooklyn Visionary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging cannot return a tapped noncreature")
    void cannotReturnTappedLand() {
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        tappedLand.tap();
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(
                player1, 0, List.of(tappedLand.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Spider-Man, Brooklyn Visionary");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The basic land search may fail to find even when a basic land is available")
    void mayFailToFindBasicLand() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new SpiderManBrooklynVisionary(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertOnBattlefield(player1, "Spider-Man, Brooklyn Visionary");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search finishes without taking a nonland when the library has no basic lands")
    void searchWithNoBasicLands() {
        SpiderManBrooklynVisionary libraryCard = new SpiderManBrooklynVisionary();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new SpiderManBrooklynVisionary(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spider-Man, Brooklyn Visionary");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
