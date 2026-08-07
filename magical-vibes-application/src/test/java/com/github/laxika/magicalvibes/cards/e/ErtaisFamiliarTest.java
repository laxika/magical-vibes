package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErtaisFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing out during the controller's untap step mills three cards")
    void phasesOutAndMillsThree() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new ErtaisFamiliar());
        stockLibrary();

        advanceTurn(); // player2's turn
        advanceTurn(); // player1's untap step — the Familiar phases out

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(familiar);

        harness.passBothPriorities(); // resolve the phase-out trigger

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Leaving the battlefield mills three cards")
    void leavesBattlefieldAndMillsThree() {
        harness.addToBattlefield(player1, new ErtaisFamiliar());
        stockLibrary();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Ertai's Familiar"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ertai's Familiar");

        harness.passBothPriorities(); // resolve the leaves-the-battlefield trigger

        // The Familiar itself plus the three milled Islands.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Ertai's Familiar");
    }

    @Test
    @DisplayName("The {U} ability stops the Familiar from phasing out at the next untap step")
    void abilityPreventsPhaseOut() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new ErtaisFamiliar());
        stockLibrary();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceTurn(); // player2's turn
        advanceTurn(); // player1's untap step — the Familiar stays put, so nothing is milled

        harness.assertOnBattlefield(player1, "Ertai's Familiar");
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(familiar);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void stockLibrary() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island(),
                new Island(), new Island(), new Island(), new Island()));
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
