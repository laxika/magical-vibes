package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialCrusader.class, EliteVanguard.class, GrizzlyBears.class})
class CelestialCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Other white creatures controlled by either player get +1/+1")
    void boostsOtherWhiteCreatures() {
        addCreatureReady(player1, new CelestialCrusader());
        Permanent ownVanguard = addCreatureReady(player1, new EliteVanguard());
        Permanent opponentVanguard = addCreatureReady(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, ownVanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownVanguard)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentVanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonwhite creatures are not boosted")
    void doesNotBoostNonwhiteCreatures() {
        addCreatureReady(player1, new CelestialCrusader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
