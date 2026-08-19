package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WellOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at your end step when you control no untapped lands")
    void drawsWithNoUntappedLands() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when you control an untapped land")
    void doesNotDrawWithUntappedLand() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Draws when all lands you control are tapped")
    void drawsWithOnlyTappedLands() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The condition is checked again when the ability resolves")
    void doesNotDrawIfLandBecomesUntappedBeforeResolution() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
