package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NightMarketLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Night Market Lookout makes each opponent lose 1 life and its controller gain 1 life")
    void tappingLookoutDrainsOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new NightMarketLookout());

        tap(lookout);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Night Market Lookout")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new NightMarketLookout());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
