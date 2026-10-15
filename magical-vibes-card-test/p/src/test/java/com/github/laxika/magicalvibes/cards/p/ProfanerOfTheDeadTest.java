package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IllusoryGains;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfanerOfTheDead.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class,
        IllusoryGains.class})
class ProfanerOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves opposing creatures unchanged")
    void decliningExploitDoesNothing() {
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castProfanerToExploitPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getPermanentId(player2, "Llanowar Elves")).isEqualTo(smallCreature.getId());
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exploit returns only opposing creatures with lower toughness")
    void exploitReturnsOnlyCreaturesWithLowerToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        castProfanerToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void sacrificingProfanerItselfStillReturnsOpposingCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());

        castProfanerToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Profaner of the Dead"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Profaner of the Dead");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void exploitedToughnessIncludesCountersBeforeLeavingBattlefield() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addToBattlefield(player2, new AirElemental());

        castProfanerToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Air Elemental");
        harness.assertOnBattlefield(player1, "Profaner of the Dead");
    }

    @Test
    void bounceTriggerBelongsToProfanersControllerAtTimeOfExploit() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent original = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gains = harness.addToBattlefieldAndReturn(player2, new IllusoryGains());
        gains.setAttachedTo(original.getId());

        castProfanerToExploitPrompt();
        harness.assertOnBattlefield(player2, "Profaner of the Dead");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Profaner of the Dead");
    }

    private void castProfanerToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ProfanerOfTheDead(), "{3}{U}");
        resolveAllTriggers();
    }
}
