package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfVipers.class, WhipstitchedZombie.class, WellOfLife.class})
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

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
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
        otherAttacker.setAttacking(true);

        blockWithWall();
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
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
