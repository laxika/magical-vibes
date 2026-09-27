package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchfiendOfDespair.class, FountainOfYouth.class, Shock.class})
class ArchfiendOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can't gain life")
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new ArchfiendOfDespair());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Controller can gain life")
    void allowsControllerLifeGain() {
        harness.addToBattlefield(player1, new ArchfiendOfDespair());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("At end step, each opponent loses life equal to life lost this turn")
    void losesLifeEqualToLifeLostThisTurn() {
        harness.addToBattlefield(player1, new ArchfiendOfDespair());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        resolveEndStep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void resolveEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
