package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LifetapTest extends BaseCardTest {

    // "Whenever a Forest an opponent controls becomes tapped, you gain 1 life."

    @Test
    @DisplayName("An opponent's Forest becoming tapped gains the controller 1 life")
    void opponentForestTapGainsLife() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(forest);

        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tapping your own Forest does not trigger (only opponents)")
    void ownForestTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(forest);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Forest land does not trigger")
    void opponentNonForestTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifetap());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
    }
}
