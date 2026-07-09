package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VeteranOfTheDepthsTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may put a +1/+1 counter on it."

    @Test
    @DisplayName("Tapping it and accepting puts a +1/+1 counter on it")
    void tappingAcceptPutsCounter() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranOfTheDepths());

        tap(veteran);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger puts no counter")
    void decliningPutsNoCounter() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranOfTheDepths());

        tap(veteran);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new VeteranOfTheDepths());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
    }
}
