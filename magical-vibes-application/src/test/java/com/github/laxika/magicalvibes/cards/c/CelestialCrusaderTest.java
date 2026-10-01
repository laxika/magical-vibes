package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheHolyNimbus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialCrusader.class, KnightOfTheHolyNimbus.class, AshcoatBear.class})
class CelestialCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Other white creatures controlled by either player get +1/+1")
    void boostsOtherWhiteCreatures() {
        Permanent crusader = addCreatureReady(player1, new CelestialCrusader());
        Permanent ownKnight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        Permanent opponentKnight = addCreatureReady(player2, new KnightOfTheHolyNimbus());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentKnight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nonwhite creatures are not boosted")
    void doesNotBoostNonwhiteCreatures() {
        addCreatureReady(player1, new CelestialCrusader());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }
}
