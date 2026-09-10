package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FogOfGnats;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarCollapse.class, GiantCockroach.class, FogOfGnats.class})
class PlanarCollapseTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, four creatures cause the enchantment to be sacrificed and all creatures destroyed")
    void sacrificesAndDestroysAllCreaturesAtFour() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());
        Permanent gnats = harness.addToBattlefieldAndReturn(player2, new FogOfGnats());
        gnats.setRegenerationShield(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Planar Collapse");
        harness.assertInGraveyard(player1, "Giant Cockroach");
        harness.assertInGraveyard(player2, "Giant Cockroach");
        harness.assertInGraveyard(player2, "Fog of Gnats");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger with fewer than four creatures on the battlefield")
    void doesNotTriggerBelowFourCreatures() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());

        advanceToUpkeep(player1);

        harness.assertOnBattlefield(player1, "Planar Collapse");
        harness.assertOnBattlefield(player1, "Giant Cockroach");
        harness.assertOnBattlefield(player2, "Giant Cockroach");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger does nothing if fewer than four creatures remain before resolution")
    void rechecksCreatureCountAtResolution() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Planar Collapse");
        assertThat(countPermanents(player1, "Giant Cockroach")).isEqualTo(2);
        assertThat(countPermanents(player2, "Giant Cockroach")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Planar Collapse");
        assertThat(countPermanents(player1, "Giant Cockroach")).isEqualTo(2);
        assertThat(countPermanents(player2, "Giant Cockroach")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Still destroys all creatures if the enchantment leaves before resolution")
    void destroysCreaturesIfSourceLeavesBeforeResolution() {
        Permanent collapse = harness.addToBattlefieldAndReturn(player1, new PlanarCollapse());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());
        harness.addToBattlefield(player2, new GiantCockroach());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(collapse);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Cockroach");
        harness.assertInGraveyard(player2, "Giant Cockroach");
        assertThat(gd.stack).isEmpty();
    }
}
