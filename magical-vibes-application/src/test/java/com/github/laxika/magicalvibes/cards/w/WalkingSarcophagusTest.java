package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
