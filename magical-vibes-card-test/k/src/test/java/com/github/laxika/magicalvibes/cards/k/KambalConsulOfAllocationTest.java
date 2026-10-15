package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FiligreeFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KambalConsulOfAllocation.class, DarkRitual.class, GrizzlyBears.class,
        FiligreeFamiliar.class, PropheticPrism.class})
class KambalConsulOfAllocationTest extends BaseCardTest {

    /** Player1 controls Kambal; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new KambalConsulOfAllocation());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's noncreature spell: that player loses 2 life and you gain 2 life")
    void opponentNoncreatureSpellDrains() {
        setUpOpponentTurn();

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent's creature spell does not trigger")
    void opponentCreatureSpellDoesNotTrigger() {
        setUpOpponentTurn();

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Controller's own noncreature spell does not trigger")
    void ownNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KambalConsulOfAllocation());

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new DarkRitual(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    void opponentArtifactSpellDrainsBeforeArtifactResolves() {
        setUpOpponentTurn();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new PropheticPrism(), "{2}");

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
    }

    @Test
    void opponentArtifactCreatureDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new FiligreeFamiliar(), "{3}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Filigree Familiar");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }

    @Test
    void triggerStillDrainsAfterKambalLeavesBattlefield() {
        setUpOpponentTurn();
        var kambal = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player2, new PropheticPrism(), "{2}");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kambal));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kambal, Consul of Allocation");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
    }
}
