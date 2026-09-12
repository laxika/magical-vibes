package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellOfDiscovery.class, RhysticCave.class})
class WellOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at your end step when you control no untapped lands")
    void drawsWithNoUntappedLands() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        harness.setLibrary(player1, List.of(new RhysticCave()));
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
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLibrary(player1, List.of(new RhysticCave()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Draws when all lands you control are tapped")
    void drawsWithOnlyTappedLands() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();
        harness.setLibrary(player1, List.of(new RhysticCave()));
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
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();
        harness.setLibrary(player1, List.of(new RhysticCave()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        land.untap();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new WellOfDiscovery());
        harness.setLibrary(player1, List.of(new RhysticCave()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
