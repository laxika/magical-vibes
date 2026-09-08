package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlanarCollapseTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, four creatures cause the enchantment to be sacrificed and all creatures destroyed")
    void sacrificesAndDestroysAllCreaturesAtFour() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        skeletons.setRegenerationShield(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Planar Collapse");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger with fewer than four creatures on the battlefield")
    void doesNotTriggerBelowFourCreatures() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        harness.assertOnBattlefield(player1, "Planar Collapse");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger does nothing if fewer than four creatures remain before resolution")
    void rechecksCreatureCountAtResolution() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Planar Collapse");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
    }
}
