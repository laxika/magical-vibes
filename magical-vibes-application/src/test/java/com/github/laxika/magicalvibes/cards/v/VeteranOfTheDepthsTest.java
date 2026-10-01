package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeteranOfTheDepths.class, HillcomberGiant.class})
class VeteranOfTheDepthsTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may put a +1/+1 counter on it."

    @Test
    @DisplayName("Tapping it and accepting puts a +1/+1 counter on it")
    void tappingAcceptPutsCounter() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranOfTheDepths());

        tap(veteran);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger puts no counter")
    void decliningPutsNoCounter() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranOfTheDepths());

        tap(veteran);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Tapping it again after untapping triggers again")
    void tappingAgainAfterUntappingTriggersAgain() {
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new VeteranOfTheDepths());

        tap(veteran);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        veteran.untap();
        tap(veteran);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(veteran.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new VeteranOfTheDepths());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
