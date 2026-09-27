package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinReplica.class, CopperMyr.class, Mountain.class})
class GoblinReplicaTest extends BaseCardTest {

    @Test
    void sacrificesItselfToDestroyTargetArtifact() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Replica");
        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertInGraveyard(player1, "Goblin Replica");
    }

    @Test
    void cannotTargetNonArtifactPermanent() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidWhenAbilityIsActivated() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Goblin Replica");
        harness.assertInGraveyard(player1, "Goblin Replica");
        harness.assertOnBattlefield(player2, "Copper Myr");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Goblin Replica");
        harness.assertNotInGraveyard(player1, "Goblin Replica");
        harness.assertOnBattlefield(player2, "Copper Myr");
    }

    @Test
    void canDestroyAnArtifactItControls() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Replica");
        harness.assertInGraveyard(player1, "Copper Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
    }

    @Test
    void fizzlesIfTargetArtifactLeavesBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new GoblinReplica());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Replica");
        harness.assertInGraveyard(player2, "Copper Myr");
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
