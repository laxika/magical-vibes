package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stenchskipper.class, PricklyBoggart.class})
class StenchskipperTest extends BaseCardTest {

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Sacrifices itself at end step when controller has no Goblins")
    void sacrificesWhenNoGoblins() {
        addCreatureReady(player1, new Stenchskipper());

        advanceToEndStep();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Stenchskipper");
        harness.assertInGraveyard(player1, "Stenchskipper");
    }

    @Test
    @DisplayName("Does not trigger when controller has a Goblin")
    void doesNotTriggerWithGoblin() {
        addCreatureReady(player1, new Stenchskipper());
        harness.addToBattlefield(player1, new PricklyBoggart());

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Stenchskipper");
    }

    @Test
    @DisplayName("Opponent's Goblins do not prevent the sacrifice")
    void opponentGoblinsDoNotHelp() {
        addCreatureReady(player1, new Stenchskipper());
        harness.addToBattlefield(player2, new PricklyBoggart());

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player1, "Stenchskipper");
    }

    @Test
    @DisplayName("Intervening-if re-checked: a Goblin entering before resolution stops the sacrifice")
    void conditionRecheckedAtResolution() {
        addCreatureReady(player1, new Stenchskipper());

        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new PricklyBoggart());

        harness.passBothPriorities(); // resolve trigger — condition no longer met

        harness.assertOnBattlefield(player1, "Stenchskipper");
    }
}
