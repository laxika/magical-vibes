package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubterraneanScoutTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target creature with power 2 or less unblockable this turn")
    void etbMakesLowPowerCreatureUnblockable() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // power 2
        harness.setHand(player1, List.of(new SubterraneanScout()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, bears.getId());

        // Resolve the creature spell → ETB trigger on the stack, then resolve the trigger.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A creature with power 3 is not a legal target")
    void cannotTargetHighPowerCreature() {
        Permanent hillGiant = addCreatureReady(player1, new HillGiant()); // power 3
        harness.setHand(player1, List.of(new SubterraneanScout()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(hillGiant.isCantBeBlocked()).isFalse();
    }
}
