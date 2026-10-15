package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DriverOfTheDead.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Incinerate.class})
class DriverOfTheDeadTest extends BaseCardTest {

    /**
     * Puts Driver of the Dead on the battlefield blocking a lethal 5/5 attacker, advances to combat
     * damage so it dies, leaving its death trigger unresolved.
     */
    private void dieInCombat() {
        Permanent driver = harness.addToBattlefieldAndReturn(player1, new DriverOfTheDead());
        driver.setSummoningSick(false);
        driver.setBlocking(true);
        driver.addBlockingTarget(0);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(5);
        bears.setToughness(5);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // Advance to combat damage so Driver of the Dead dies.
    }

    @Test
    @DisplayName("Dies: returns a chosen mana value 2 or less creature card to the battlefield")
    void diesReturnsCheapCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dieInCombat();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        // Driver of the Dead has mana value 4 and cannot target itself.
        harness.assertInGraveyard(player1, "Driver of the Dead");
    }

    @Test
    @DisplayName("Dies: creature cards with mana value 3 or more are not eligible")
    void diesIgnoresExpensiveCreature() {
        harness.setGraveyard(player1, List.of(new HillGiant()));

        dieInCombat();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Dies: controller picks among multiple eligible creature cards")
    void diesPicksAmongEligibleCards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new HillGiant(), new LlanowarElves(), bears));

        dieInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Death trigger requires choosing a target before it can resolve")
    void choosesTargetBeforeResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dieInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returning an eligible creature cannot be declined")
    void cannotDeclineReturn() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dieInCombat();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature cards are not eligible even with mana value 2")
    void ignoresCheapNoncreature() {
        harness.setGraveyard(player1, List.of(new Incinerate()));

        dieInCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Incinerate");
        harness.assertNotOnBattlefield(player1, "Incinerate");
    }

    @Test
    @DisplayName("An opponent's graveyard cannot provide the return target")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        dieInCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
