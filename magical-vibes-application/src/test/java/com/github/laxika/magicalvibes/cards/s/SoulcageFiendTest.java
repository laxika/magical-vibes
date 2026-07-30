package com.github.laxika.magicalvibes.cards.s;

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

class SoulcageFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Soulcage Fiend puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new SoulcageFiend()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Soulcage Fiend");
    }

    @Test
    @DisplayName("When Soulcage Fiend dies, its trigger goes on the stack")
    void deathTriggerGoesOnStack() {
        harness.addToBattlefield(player1, new SoulcageFiend());

        setupCombatWhereFiendDies();
        harness.passBothPriorities(); // Combat damage — Soulcage Fiend dies

        harness.assertInGraveyard(player1, "Soulcage Fiend");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Soulcage Fiend");
    }

    @Test
    @DisplayName("Resolving the death trigger makes each player, including the controller, lose 3 life")
    void deathTriggerCausesEachPlayerLifeLoss() {
        harness.addToBattlefield(player1, new SoulcageFiend());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        setupCombatWhereFiendDies();
        harness.passBothPriorities(); // Combat damage — Soulcage Fiend dies
        harness.passBothPriorities(); // Resolve the death trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("No life is lost while Soulcage Fiend stays on the battlefield")
    void noLifeLossWhileAlive() {
        harness.addToBattlefield(player1, new SoulcageFiend());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    /**
     * Sets up combat where Soulcage Fiend (player1, 3/2) attacks and is blocked by a 5/5 creature
     * (player2), so the Fiend dies to combat damage.
     */
    private void setupCombatWhereFiendDies() {
        Permanent fiendPerm = findPermanent(player1, "Soulcage Fiend");
        fiendPerm.setSummoningSick(false);
        fiendPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(5);
        bigBear.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
