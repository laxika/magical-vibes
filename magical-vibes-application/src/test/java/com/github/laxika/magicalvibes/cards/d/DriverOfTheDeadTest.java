package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DriverOfTheDeadTest extends BaseCardTest {

    /**
     * Puts Driver of the Dead on the battlefield blocking a lethal 5/5 attacker, advances to combat
     * damage so it dies, then resolves the queued death trigger.
     */
    private void killInCombat() {
        Permanent driver = new Permanent(new DriverOfTheDead());
        driver.setSummoningSick(false);
        driver.setBlocking(true);
        driver.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(driver);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(5);
        bears.setToughness(5);
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to combat damage → Driver of the Dead dies
        harness.passBothPriorities(); // resolve death trigger
    }

    @Test
    @DisplayName("Dies: returns a chosen mana value 2 or less creature card to the battlefield")
    void diesReturnsCheapCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        killInCombat();

        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        // Driver of the Dead itself costs {3}{B} — it can never be its own return target
        harness.assertInGraveyard(player1, "Driver of the Dead");
    }

    @Test
    @DisplayName("Dies: creature cards with mana value 3 or more are not eligible")
    void diesIgnoresExpensiveCreature() {
        harness.setGraveyard(player1, List.of(new HillGiant()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Dies: controller picks among multiple eligible creature cards")
    void diesPicksAmongEligibleCards() {
        harness.setGraveyard(player1, List.of(new HillGiant(), new LlanowarElves(), new GrizzlyBears()));

        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Hill Giant");
    }
}
