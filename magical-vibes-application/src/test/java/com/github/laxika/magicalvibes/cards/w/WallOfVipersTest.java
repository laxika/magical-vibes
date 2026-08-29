package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfVipersTest extends BaseCardTest {

    @Test
    @DisplayName("{3}: destroys Wall of Vipers and the creature it is blocking")
    void destroysWallAndBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfVipers());

        blockWithWall();
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.activateAbility(player2, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Wall of Vipers");
    }

    @Test
    @DisplayName("Any player may activate Wall of Vipers's ability")
    void anyPlayerMayActivateAbility() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new WallOfVipers());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.activateAbility(player2, 1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Wall of Vipers");
    }

    @Test
    @DisplayName("Ability cannot target a creature Wall of Vipers is not blocking")
    void cannotTargetUnblockedCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WallOfVipers());
        otherAttacker.setAttacking(true);

        blockWithWall();
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, otherAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void blockWithWall() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
