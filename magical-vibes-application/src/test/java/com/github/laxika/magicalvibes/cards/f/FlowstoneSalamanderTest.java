package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowstoneSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: deals 1 damage to a creature blocking Flowstone Salamander")
    void damagesBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly for more damage")
    void damageStacksAcrossActivations() {
        addCreatureReady(player1, new FlowstoneSalamander());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that isn't blocking Flowstone Salamander")
    void cannotTargetNonBlocker() {
        addCreatureReady(player1, new FlowstoneSalamander());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        blockSalamander();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Attacks with the Salamander and blocks it with player2's first creature. */
    private void blockSalamander() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
