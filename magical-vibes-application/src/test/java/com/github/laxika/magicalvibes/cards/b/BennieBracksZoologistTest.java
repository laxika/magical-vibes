package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NuisanceEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BennieBracksZoologist.class, NuisanceEngine.class})
class BennieBracksZoologistTest extends BaseCardTest {

    @Test
    void drawsAtEndStepAfterCreatingAToken() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep + 1);
    }

    @Test
    void doesNotDrawWithoutCreatingAToken() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep);
    }

    @Test
    void triggersAtEachPlayersEndStep() {
        harness.addToBattlefield(player1, new BennieBracksZoologist());
        harness.addToBattlefield(player1, new NuisanceEngine());
        harness.setLibrary(player1, List.of(new BennieBracksZoologist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        int handSizeBeforeEndStep = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeEndStep + 1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
