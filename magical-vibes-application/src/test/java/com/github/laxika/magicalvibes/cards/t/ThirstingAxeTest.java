package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThirstingAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself if the equipped creature did not deal combat damage to a creature")
    void sacrificesAtEndStepWithoutCombatDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        advanceToEndStep();

        harness.assertInGraveyard(player1, "Thirsting Axe");
    }

    @Test
    @DisplayName("Combat damage to a player does not satisfy the sacrifice condition")
    void combatDamageToPlayerDoesNotSatisfyCondition() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        advanceToEndStep();

        harness.assertInGraveyard(player1, "Thirsting Axe");
    }

    @Test
    @DisplayName("Does not sacrifice itself if the equipped creature dealt combat damage to a creature")
    void combatDamageToCreaturePreventsSacrifice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new WallOfWood());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Thirsting Axe");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addAxeReady(Player player) {
        Permanent axe = new Permanent(new ThirstingAxe());
        axe.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(axe);
        return axe;
    }
}
