package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({NighthawkScavenger.class, Forest.class, GrizzlyBears.class, LavaSpike.class,
        Millstone.class, Ornithopter.class, Shock.class})
class NighthawkScavengerTest extends BaseCardTest {

    @Test
    @DisplayName("Has 1/3 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts distinct card types in opponents' graveyards")
    void countsDistinctCardTypesInOpponentsGraveyards() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone(), new LavaSpike(),
                new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scavenger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates when opponents' graveyard card types change")
    void updatesWhenOpponentsGraveyardCardTypesChange() {
        Permanent scavenger = addCreatureReady(player1, new NighthawkScavenger());

        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock(), new Millstone()));
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(4);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, scavenger)).isEqualTo(1);
    }
}
