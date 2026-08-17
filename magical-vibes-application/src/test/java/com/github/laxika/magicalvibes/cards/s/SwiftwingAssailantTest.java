package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftwingAssailantTest extends BaseCardTest {

    @Test
    void gainsToughnessAndVigilanceAtMaxSpeed() {
        Permanent assailant = addCreatureReady(player1, new SwiftwingAssailant());
        int toughnessBeforeMaxSpeed = gqs.getEffectiveToughness(gd, assailant);

        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isFalse();

        gd.playerSpeeds.put(player1.getId(), 4);

        assertThat(gqs.getEffectiveToughness(gd, assailant)).isEqualTo(toughnessBeforeMaxSpeed + 1);
        assertThat(gqs.hasKeyword(gd, assailant, Keyword.VIGILANCE)).isTrue();
    }
}
