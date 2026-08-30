package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MoonriseCleric.class)
class MoonriseClericTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when it attacks")
    void gainsLifeWhenAttacking() {
        addCreatureReady(player1, new MoonriseCleric());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Does not gain life when it does not attack")
    void doesNotGainLifeWhenNotAttacking() {
        addCreatureReady(player1, new MoonriseCleric());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
