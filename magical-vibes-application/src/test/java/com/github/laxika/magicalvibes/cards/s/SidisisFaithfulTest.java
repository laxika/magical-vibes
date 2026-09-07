package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SidisisFaithful.class, GrizzlyBears.class, Forest.class})
class SidisisFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("Exploiting a creature returns the target creature to its owner's hand")
    void exploitReturnsTargetCreatureToHand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castFaithfulToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sidisi's Faithful");
    }

    @Test
    @DisplayName("Declining exploit does not return a creature")
    void decliningExploitDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castFaithfulToExploitPrompt();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sidisi's Faithful");
    }

    @Test
    @DisplayName("Exploit ability cannot target a noncreature permanent")
    void exploitRejectsNoncreatureTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castFaithfulToExploitPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFaithfulToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SidisisFaithful()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
