package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WylieDukeAtiinHero.class, GrizzlyBears.class})
class WylieDukeAtiinHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped gains 1 life and draws a card")
    void becomingTappedGainsLifeAndDrawsCard() {
        Permanent wylie = harness.addToBattlefieldAndReturn(player1, new WylieDukeAtiinHero());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(wylie);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Wylie Duke")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WylieDukeAtiinHero());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
