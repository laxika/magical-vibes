package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpendingDisaster.class, TreetopVillage.class, GiantCockroach.class})
class ImpendingDisasterTest extends BaseCardTest {

    @Test
    @DisplayName("Seven lands on the battlefield sacrifice Impending Disaster and destroy all lands")
    void sevenLandsSacrificeAndDestroyAllLands() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new GiantCockroach());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(disaster);
        assertThat(countPermanents(player1, "Treetop Village")).isZero();
        assertThat(countPermanents(player2, "Treetop Village")).isZero();
        harness.assertOnBattlefield(player1, "Giant Cockroach");
        harness.assertInGraveyard(player1, "Impending Disaster");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fewer than seven lands do not trigger Impending Disaster")
    void fewerThanSevenLandsDoNotTrigger() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(disaster);
        assertThat(countPermanents(player1, "Treetop Village")).isEqualTo(3);
        assertThat(countPermanents(player2, "Treetop Village")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger rechecks the land count when it resolves")
    void rechecksLandCountAtResolution() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(disaster);
        assertThat(countPermanents(player1, "Treetop Village")).isEqualTo(4);
        assertThat(countPermanents(player2, "Treetop Village")).isEqualTo(2);
        harness.assertInGraveyard(player2, "Treetop Village");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Impending Disaster triggers only during its controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(disaster);
        assertThat(countPermanents(player1, "Treetop Village")).isEqualTo(4);
        assertThat(countPermanents(player2, "Treetop Village")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The triggered ability still destroys all lands if Impending Disaster leaves before resolution")
    void destroysLandsIfSourceLeavesBeforeResolution() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player1, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());
        harness.addToBattlefield(player2, new TreetopVillage());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, disaster));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treetop Village")).isZero();
        assertThat(countPermanents(player2, "Treetop Village")).isZero();
        harness.assertInGraveyard(player1, "Impending Disaster");
        assertThat(gd.stack).isEmpty();
    }
}
