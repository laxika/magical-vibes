package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoneOfFlame.class, Replenish.class})
class ZoneOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each opponent when it enters the enchanted battlefield")
    void triggersOnBattlefieldEntries() {
        harness.castFromHand(player1, new ZoneOfFlame(), "{4}{R}{R}{R}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void triggersForAnOpponentsCardEnteringTheBattlefield() {
        harness.addToBattlefield(player1, new ZoneOfFlame());

        harness.enterBattlefieldAndReturn(player2, new ZoneOfFlame());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void triggersForAnotherCardLeavingTheBattlefield() {
        harness.addToBattlefield(player1, new ZoneOfFlame());
        var leaving = harness.addToBattlefieldAndReturn(player2, new ZoneOfFlame());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, leaving));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void triggersWhenItLeavesTheEnchantedBattlefield() {
        var source = harness.addToBattlefieldAndReturn(player1, new ZoneOfFlame());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void tokenCopiesEnteringDoNotTrigger() {
        harness.addToBattlefield(player1, new ZoneOfFlame());
        var tokenCopy = new ZoneOfFlame();
        tokenCopy.setToken(true);

        harness.enterBattlefieldAndReturn(player2, tokenCopy);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void tokenCopiesLeavingDoNotTrigger() {
        harness.addToBattlefield(player1, new ZoneOfFlame());
        var tokenCopy = new ZoneOfFlame();
        tokenCopy.setToken(true);
        var leaving = harness.addToBattlefieldAndReturn(player2, tokenCopy);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, leaving));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void simultaneousCardEntriesTriggerEachSourceOnlyOnce() {
        harness.addToBattlefield(player1, new ZoneOfFlame());
        harness.setGraveyard(player1, List.of(new ZoneOfFlame(), new ZoneOfFlame()));

        harness.castFromHand(player1, new Replenish(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
