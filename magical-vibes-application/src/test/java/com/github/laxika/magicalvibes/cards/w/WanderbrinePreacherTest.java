package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WanderbrinePreacherTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Wanderbrine Preacher gains 2 life")
    void tappingWanderbrinePreacherGainsLife() {
        Permanent preacher = harness.addToBattlefieldAndReturn(player1, new WanderbrinePreacher());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        tap(preacher);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Wanderbrine Preacher")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new WanderbrinePreacher());
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
