package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StonybrookSchoolmasterTest extends BaseCardTest {

    // "Whenever this creature becomes tapped, you may create a 1/1 blue Merfolk Wizard creature token."

    @Test
    @DisplayName("Tapping it and accepting creates a Merfolk Wizard token")
    void tappingAcceptCreatesToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger creates no token")
    void decliningCreatesNoToken() {
        Permanent schoolmaster = harness.addToBattlefieldAndReturn(player1, new StonybrookSchoolmaster());

        tap(schoolmaster);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tokenCount()).isZero();
    }

    @Test
    @DisplayName("Tapping another creature you control does not trigger")
    void tappingOtherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new StonybrookSchoolmaster());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        tap(other);

        assertThat(gd.stack).isEmpty();
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Merfolk Wizard".equals(p.getCard().getName()))
                .count();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent);
    }
}
