package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tarmogoyf.class, Forest.class, GrizzlyBears.class, LavaSpike.class, Millstone.class,
        Ornithopter.class, Shock.class})
class TarmogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Has 0/1 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());

        assertThat(gqs.getEffectivePower(gd, goyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power counts distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone(), new LavaSpike(),
                new Ornithopter()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("Power and toughness update as graveyard card types change")
    void updatesWhenGraveyardCardTypesChange() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);
    }
}
