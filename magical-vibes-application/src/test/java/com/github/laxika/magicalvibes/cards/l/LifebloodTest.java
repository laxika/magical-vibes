package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifeblood.class, Mountain.class, Island.class})
class LifebloodTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's Mountain becoming tapped gains the controller 1 life")
    void opponentMountainTapGainsLife() {
        harness.addToBattlefield(player1, new Lifeblood());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(mountain);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Tapping your own Mountain does not trigger")
    void ownMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(mountain);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping an opponent's non-Mountain land does not trigger")
    void opponentNonMountainTapDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lifeblood());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(island);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
