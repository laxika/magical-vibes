package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WalkingSarcophagus.class})
class WalkingSarcophagusTest extends BaseCardTest {

    @Test
    void getsBoostAtMaxSpeed() {
        Permanent sarcophagus = addCreatureReady(player1, new WalkingSarcophagus());

        assertThat(gqs.getEffectivePower(gd, sarcophagus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarcophagus)).isEqualTo(1);

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, sarcophagus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarcophagus)).isEqualTo(3);
    }

    @Test
    void losesBoostWhenNoLongerAtMaxSpeed() {
        Permanent sarcophagus = addCreatureReady(player1, new WalkingSarcophagus());
        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, sarcophagus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarcophagus)).isEqualTo(3);

        gd.playerSpeeds.put(player1.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, sarcophagus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sarcophagus)).isEqualTo(1);
    }

    @Test
    void startsEnginesWhenCastAndResolved() {
        harness.castFromHand(player1, new WalkingSarcophagus(), "{2}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Sarcophagus");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void enteringDoesNotResetExistingMaxSpeed() {
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.castFromHand(player1, new WalkingSarcophagus(), "{2}");
        harness.passBothPriorities();

        Permanent sarcophagus = findPermanent(player1, "Walking Sarcophagus");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, sarcophagus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sarcophagus)).isEqualTo(3);
    }

    @Test
    void onlyControllersMaxSpeedAppliesAndCopiesDoNotBoostEachOther() {
        Permanent first = addCreatureReady(player1, new WalkingSarcophagus());
        Permanent second = addCreatureReady(player1, new WalkingSarcophagus());
        Permanent opposing = addCreatureReady(player2, new WalkingSarcophagus());
        gd.playerSpeeds.put(player1.getId(), 3);
        gd.playerSpeeds.put(player2.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }
}
