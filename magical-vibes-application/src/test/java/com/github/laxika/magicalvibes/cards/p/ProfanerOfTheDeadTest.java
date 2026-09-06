package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfanerOfTheDead.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
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

    private void castProfanerToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ProfanerOfTheDead()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
