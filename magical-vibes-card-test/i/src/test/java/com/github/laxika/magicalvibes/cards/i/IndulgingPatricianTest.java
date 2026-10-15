package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IndulgingPatrician.class, Revitalize.class})
class IndulgingPatricianTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Each opponent loses 3 life at your end step after gaining at least 3 life")
    void eachOpponentLosesLifeAtThreshold() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    @DisplayName("Does not trigger when fewer than 3 life was gained")
    void doesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 2);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);
        int startingLife = gd.getLife(player2.getId());

        advanceToEndStep(player2);
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
    }

    @Test
    void lifeGainedBeforePatricianEnteredStillCountsDespiteLaterLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new IndulgingPatrician()));
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();
        harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 5, "test life loss");
        harness.addToBattlefield(player1, new IndulgingPatrician());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 17);
    }

    @Test
    void gainingLifeAfterEndStepBeginsDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new IndulgingPatrician()));

        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        harness.castFromHand(player1, new Revitalize(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.getLifeGainedThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void multiplePatriciansTriggerIndependentlyWithoutDrainingController() {
        harness.addToBattlefield(player1, new IndulgingPatrician());
        harness.addToBattlefield(player1, new IndulgingPatrician());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }
}
