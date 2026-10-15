package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.cards.d.Deathgreeter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfVipers.class, WhipstitchedZombie.class, WellOfLife.class, Withdraw.class,
        Cytoshape.class, Deathgreeter.class})
class WallOfVipersTest extends BaseCardTest {

    @Test
    @DisplayName("{3}: destroys Wall of Vipers and the creature it is blocking")
    void destroysWallAndBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());

        blockWithWall();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Whipstitched Zombie");
        harness.assertInGraveyard(player2, "Wall of Vipers");
    }

    @Test
    @DisplayName("Any player may activate Wall of Vipers's ability")
    void anyPlayerMayActivateAbility() {
        Permanent attacker = addCreatureReady(player2, new WhipstitchedZombie());
        addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player1, new WallOfVipers());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Whipstitched Zombie");
        harness.assertInGraveyard(player1, "Wall of Vipers");
    }

    @Test
    @DisplayName("Ability cannot target a creature Wall of Vipers is not blocking")
    void cannotTargetUnblockedCreature() {
        addCreatureReady(player1, new WhipstitchedZombie());
        Permanent otherAttacker = addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());
        harness.addToBattlefield(player1, new WellOfLife());

        blockWithWall();
        Permanent noncreature = findPermanent(player1, "Well of Life");
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
        harness.assertOnBattlefield(player1, "Well of Life");
        harness.assertOnBattlefield(player2, "Wall of Vipers");
    }

    @Test
    @DisplayName("Ability requires three mana")
    void cannotActivateWithoutThreeMana() {
        Permanent attacker = addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());

        blockWithWall();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
        harness.assertOnBattlefield(player2, "Wall of Vipers");
    }

    private void blockWithWall() {
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("Cannot activate the ability while Wall of Vipers is not blocking")
    void cannotActivateOutsideCombat() {
        Permanent creature = addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Whipstitched Zombie");
        harness.assertOnBattlefield(player2, "Wall of Vipers");
    }

    @Test
    @DisplayName("Wall survives when its only target leaves before resolution")
    void illegalTargetPreventsBothDestructions() {
        Permanent attacker = addCreatureReady(player1, new WhipstitchedZombie());
        Permanent bystander = addCreatureReady(player1, new WhipstitchedZombie());
        addCreatureReady(player2, new WallOfVipers());

        blockWithWall();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(attacker.getId(), bystander.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Whipstitched Zombie");
        harness.assertOnBattlefield(player2, "Wall of Vipers");
        harness.assertNotInGraveyard(player2, "Wall of Vipers");
    }

    @Test
    @DisplayName("The blocked creature is still destroyed after Wall leaves the battlefield")
    void sourceLeavingDoesNotStopAbility() {
        Permanent attacker = addCreatureReady(player1, new WhipstitchedZombie());
        Permanent bystander = addCreatureReady(player1, new WhipstitchedZombie());
        Permanent wall = addCreatureReady(player2, new WallOfVipers());

        blockWithWall();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(wall.getId(), bystander.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Wall of Vipers");
        harness.assertNotInGraveyard(player2, "Wall of Vipers");
        harness.assertInGraveyard(player1, "Whipstitched Zombie");
    }

    @Test
    @DisplayName("Both destructions are simultaneous for death triggers gained before resolution")
    void simultaneousDestructionPreservesDyingSourcesTriggers() {
        Permanent attacker = addCreatureReady(player1, new WhipstitchedZombie());
        Permanent wall = addCreatureReady(player2, new WallOfVipers());
        Permanent deathgreeter = addCreatureReady(player2, new Deathgreeter());

        blockWithWall();
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.setHand(player1, List.of(new Cytoshape()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, wall.getId());
        harness.handlePermanentChosen(player1, deathgreeter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Whipstitched Zombie");
        harness.assertInGraveyard(player2, "Wall of Vipers");
        assertThat(gd.stack).hasSize(3);

        for (int i = 0; i < 3; i++) {
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player2, true);
        }
        harness.assertLife(player2, 23);
    }
}
