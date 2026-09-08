package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeathsShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Gets -X/-X based on its controller's life total")
    void getsMinusLifeTotal() {
        gd.playerLifeTotals.put(player1.getId(), 8);
        Permanent shadow = addCreatureReady(player1, new DeathsShadow());

        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(5);
    }

    @Test
    @DisplayName("Updates as its controller's life total changes")
    void updatesWithLifeTotal() {
        gd.playerLifeTotals.put(player1.getId(), 8);
        Permanent shadow = addCreatureReady(player1, new DeathsShadow());

        gd.playerLifeTotals.put(player1.getId(), 3);

        assertThat(gqs.getEffectivePower(gd, shadow)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, shadow)).isEqualTo(10);
    }
}
