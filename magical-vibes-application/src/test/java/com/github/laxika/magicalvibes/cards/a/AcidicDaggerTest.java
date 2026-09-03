package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RecklessEmbermage;
import com.github.laxika.magicalvibes.cards.u.UrborgPanther;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcidicDagger.class, GiantMantis.class, Mountain.class, RecklessEmbermage.class,
        UrborgPanther.class, WallOfRoots.class})
class AcidicDaggerTest extends BaseCardTest {

    private void enterDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    private void activateDagger(Permanent dagger, Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, indexOf(dagger), 0, null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A non-Wall creature damaged in combat by the targeted creature is destroyed")
    void destroysDamagedNonWallCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());
        addCreatureReady(player2, new GiantMantis()); // 2/4 survives Urborg Panther's 2 damage

        panther.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, panther);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve the Dagger's delayed trigger

        harness.assertNotOnBattlefield(player2, "Giant Mantis");
        harness.assertInGraveyard(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("A Wall damaged in combat by the targeted creature is not destroyed")
    void doesNotDestroyDamagedWall() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());
        addCreatureReady(player2, new WallOfRoots()); // 0/5 survives Urborg Panther's 2 damage

        panther.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, panther);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Roots");
    }

    @Test
    @DisplayName("A creature damaged by an untargeted attacker is not destroyed")
    void doesNotDestroyCreatureDamagedByOtherCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent other = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());
        addCreatureReady(player2, new GiantMantis());

        other.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, panther); // the non-attacking Panther carries the trigger

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("Noncombat damage from the targeted creature does not trigger destruction")
    void doesNotDestroyCreatureFromNoncombatDamage() {
        Permanent embermage = addCreatureReady(player1, new RecklessEmbermage());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());
        Permanent mantis = addCreatureReady(player2, new GiantMantis());

        enterDeclareAttackers();
        activateDagger(dagger, embermage);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, indexOf(embermage), 0, null, mantis.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Giant Mantis");
    }

    @Test
    @DisplayName("A target that leaves before resolution does not register delayed effects")
    void targetLeavingBeforeResolutionDoesNotRegisterDelayedEffects() {
        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());

        enterDeclareAttackers();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, indexOf(dagger), 0, null, panther.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, panther));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Acidic Dagger");
        harness.assertInHand(player1, "Urborg Panther");
    }

    @Test
    @DisplayName("Sacrifices itself when the targeted creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());

        enterDeclareAttackers();
        activateDagger(dagger, panther);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, panther));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Acidic Dagger");
        harness.assertInGraveyard(player1, "Acidic Dagger");
    }

    @Test
    @DisplayName("Cannot be activated once blockers have been declared")
    void cannotActivateAfterBlockersDeclared() {
        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(dagger), 0, null, panther.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated before blockers are declared in a later combat phase")
    void cannotActivateDuringLaterCombatPhase() {
        Permanent panther = addCreatureReady(player1, new UrborgPanther());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.combatPhasesThisTurn = 2;
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(dagger), 0, null, panther.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent dagger = addCreatureReady(player1, new AcidicDagger());

        enterDeclareAttackers();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(dagger), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
