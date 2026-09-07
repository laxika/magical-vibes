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
        harness.setHand(player1, List.of(new SpiderManBrooklynVisionary()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
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
}
