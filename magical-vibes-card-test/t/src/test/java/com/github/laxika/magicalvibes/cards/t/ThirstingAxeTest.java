package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstingAxe.class, GrizzlyBears.class, WallOfWood.class})
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
    @DisplayName("Sacrifices the equipped creature if it did not deal combat damage to a creature")
    void sacrificesAtEndStepWithoutCombatDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        advanceToEndStep();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Thirsting Axe");
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

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Thirsting Axe");
    }

    @Test
    @DisplayName("Does not sacrifice the equipped creature if it dealt combat damage to a creature")
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
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void equipAttachesForTwoMana() {
        Permanent axe = addAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
    }

    @Test
    void unattachedAxeDoesNotTriggerOrBoostCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAxeReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Thirsting Axe");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsEndStepDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Thirsting Axe");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeEquippedCreatureControlledByOpponent() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Thirsting Axe");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void combatDamageBeforeEquippingStillPreventsSacrifice() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfWood());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(creature.getId());
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Thirsting Axe");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reequippingAfterCombatChecksNewBearer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent newBearer = addCreatureReady(player1, new GrizzlyBears());
        Permanent axe = addAxeReady(player1);
        axe.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfWood());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        axe.setAttachedTo(newBearer.getId());
        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker).doesNotContain(newBearer);
        harness.assertOnBattlefield(player1, "Thirsting Axe");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private Permanent addAxeReady(Player player) {
        Permanent axe = harness.addToBattlefieldAndReturn(player, new ThirstingAxe());
        axe.setSummoningSick(false);
        return axe;
    }
}
