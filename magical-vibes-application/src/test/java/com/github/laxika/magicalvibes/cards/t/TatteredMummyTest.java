package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TatteredMummyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Tattered Mummy puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new TatteredMummy()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tattered Mummy");
    }

    @Test
    @DisplayName("When Tattered Mummy dies, its trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new TatteredMummy());

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Combat damage — Tattered Mummy dies

        harness.assertInGraveyard(player1, "Tattered Mummy");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Tattered Mummy");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each opponent lose 2 life, not the controller")
    void deathTriggerCausesOpponentLifeLoss() {
        harness.addToBattlefield(player1, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereMummyDies();
        harness.passBothPriorities(); // Combat damage — Tattered Mummy dies
        harness.passBothPriorities(); // Resolve the death trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No life is lost while Tattered Mummy stays on the battlefield")
    void noLifeLossWhileAlive() {
        harness.addToBattlefield(player1, new TatteredMummy());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    /**
     * Sets up combat where Tattered Mummy (player1, 1/2) attacks and is blocked by a 2/2 creature
     * (player2), so the Mummy dies to combat damage.
     */
    private void setupCombatWhereMummyDies() {
        Permanent mummyPerm = findPermanent(player1, "Tattered Mummy");
        mummyPerm.setSummoningSick(false);
        mummyPerm.setAttacking(true);

        Permanent blockerPerm = new Permanent(new GrizzlyBears());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
