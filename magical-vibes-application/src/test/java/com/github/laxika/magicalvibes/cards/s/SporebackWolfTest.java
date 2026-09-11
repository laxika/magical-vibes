package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SporebackWolf.class)
class SporebackWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+2 during its controller's turn")
    void getsToughnessBoostDuringControllersTurn() {
        Permanent wolf = addCreatureReady(player1, new SporebackWolf());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the toughness boost during its controller's opponent's turn")
    void noToughnessBoostDuringOpponentsTurn() {
        Permanent wolf = addCreatureReady(player1, new SporebackWolf());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }
}
