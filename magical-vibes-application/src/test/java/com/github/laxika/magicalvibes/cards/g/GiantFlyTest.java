package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantFly.class, AuraOfSilence.class, AngelsFeather.class})
class GiantFlyTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when you sacrifice another permanent")
    void getsPowerForSacrificedPermanent() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.sacrificePermanent(player1, 1, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, giantFly)).isEqualTo(2);
    }

    @Test
    @DisplayName("The sacrifice bonus expires at end of turn")
    void bonusExpiresAtEndOfTurn() {
        Permanent giantFly = addCreatureReady(player1, new GiantFly());
        harness.addToBattlefield(player1, new AuraOfSilence());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());

        harness.sacrificePermanent(player1, 1, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(3);

        gd.expireEndOfTurnFloatingEffects();
        giantFly.resetModifiers();

        assertThat(harness.getGameQueryService().getEffectivePower(gd, giantFly)).isEqualTo(2);
    }
}
