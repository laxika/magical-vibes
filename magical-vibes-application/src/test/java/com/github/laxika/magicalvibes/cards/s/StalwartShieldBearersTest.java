package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stalwart Shield-Bearers")
class StalwartShieldBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other defending creatures you control")
    void boostsOtherDefendingCreaturesYouControl() {
        Permanent shieldBearers = harness.addToBattlefieldAndReturn(player1, new StalwartShieldBearers());
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new SentryOak());
        Permanent nonDefender = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDefender = harness.addToBattlefieldAndReturn(player2, new SentryOak());

        assertThat(gqs.getEffectiveToughness(gd, shieldBearers)).isEqualTo(shieldBearers.getCard().getToughness());
        assertThat(gqs.getEffectiveToughness(gd, defender)).isEqualTo(defender.getCard().getToughness() + 2);
        assertThat(gqs.getEffectiveToughness(gd, nonDefender)).isEqualTo(nonDefender.getCard().getToughness());
        assertThat(gqs.getEffectiveToughness(gd, opponentDefender))
                .isEqualTo(opponentDefender.getCard().getToughness());
    }
}
