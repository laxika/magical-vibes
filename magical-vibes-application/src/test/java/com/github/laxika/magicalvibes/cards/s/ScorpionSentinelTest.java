package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScorpionSentinel.class, Forest.class})
class ScorpionSentinelTest extends BaseCardTest {

    @Test
    void remainsBaseStatsBelowSevenLands() {
        addLands(player1, 6);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    void getsPlusThreePowerAtSevenLands() {
        addLands(player1, 7);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    void losesBoostWhenControllerDropsBelowSevenLands() {
        addLands(player1, 7);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Forest);

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    @Test
    void opponentsLandsDoNotCount() {
        addLands(player2, 7);
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(4);
    }

    private void addLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
