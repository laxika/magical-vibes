package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThoughtleechTest extends BaseCardTest {

    // "Whenever an Island an opponent controls becomes tapped, you may gain 1 life."

    @Test
    @DisplayName("An opponent's Island becoming tapped lets the controller gain 1 life")
    void opponentIslandTapAcceptGainsLife() {
        harness.addToBattlefield(player1, new Thoughtleech());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void decliningGainsNoLife() {
        harness.addToBattlefield(player1, new Thoughtleech());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping your own Island does not trigger (only opponents)")
    void ownIslandTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Thoughtleech());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Island land does not trigger")
    void opponentNonIslandTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Thoughtleech());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(forest);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
    }
}
