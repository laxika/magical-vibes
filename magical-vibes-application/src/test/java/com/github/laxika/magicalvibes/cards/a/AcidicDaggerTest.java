package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcidicDaggerTest extends BaseCardTest {

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

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

        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent dagger = addReady(player1, new AcidicDagger());
        addReady(player2, new GiantSpider()); // 2/4 survives Grizzly Bears' 2 damage

        bears.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // resolve the Dagger's delayed trigger

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A Wall damaged in combat by the targeted creature is not destroyed")
    void doesNotDestroyDamagedWall() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent dagger = addReady(player1, new AcidicDagger());
        addReady(player2, new WallOfWood()); // 0/3 survives Grizzly Bears' 2 damage

        bears.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("A creature damaged by an untargeted attacker is not destroyed")
    void doesNotDestroyCreatureDamagedByOtherCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent other = addReady(player1, new GrizzlyBears());
        Permanent dagger = addReady(player1, new AcidicDagger());
        addReady(player2, new GiantSpider());

        other.setAttacking(true);
        enterDeclareAttackers();
        activateDagger(dagger, bears); // the non-attacking Bears carries the trigger

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Sacrifices itself when the targeted creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent dagger = addReady(player1, new AcidicDagger());

        enterDeclareAttackers();
        activateDagger(dagger, bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Acidic Dagger");
        harness.assertInGraveyard(player1, "Acidic Dagger");
    }

    @Test
    @DisplayName("Cannot be activated once blockers have been declared")
    void cannotActivateAfterBlockersDeclared() {
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent dagger = addReady(player1, new AcidicDagger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(dagger), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mountain = addReady(player1, new Mountain());
        Permanent dagger = addReady(player1, new AcidicDagger());

        enterDeclareAttackers();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(dagger), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
