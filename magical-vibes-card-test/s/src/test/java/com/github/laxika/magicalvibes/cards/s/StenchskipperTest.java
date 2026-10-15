package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Stenchskipper.class, PricklyBoggart.class, WingsOfVelisVel.class})
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

    @Test
    @DisplayName("Sacrifices itself on the opponent's end step too")
    void sacrificesOnOpponentsEndStep() {
        addCreatureReady(player1, new Stenchskipper());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stenchskipper");
        harness.assertInGraveyard(player1, "Stenchskipper");
    }

    @Test
    @DisplayName("Does not trigger when Stenchskipper itself is a Goblin")
    void doesNotTriggerWhenSourceIsGoblin() {
        addCreatureReady(player1, new Stenchskipper());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Stenchskipper"));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Stenchskipper");
    }

    @Test
    @DisplayName("Becoming a Goblin in response prevents Stenchskipper's sacrifice")
    void sourceBecomingGoblinStopsSacrificeAtResolution() {
        addCreatureReady(player1, new Stenchskipper());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        advanceToEndStep();
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Stenchskipper"));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stenchskipper");
        harness.assertNotInGraveyard(player1, "Stenchskipper");
    }
}
