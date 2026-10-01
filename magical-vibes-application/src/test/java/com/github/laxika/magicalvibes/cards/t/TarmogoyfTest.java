package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CloudKey;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.j.JudgeUnworthy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tarmogoyf.class, DryadArbor.class, CloudKey.class, JudgeUnworthy.class, Foresee.class,
        Imperiosaur.class})
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
                new Imperiosaur(), new DryadArbor(), new JudgeUnworthy(), new CloudKey(), new Foresee()));
        harness.setGraveyard(player2, List.of(new Imperiosaur(), new JudgeUnworthy()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("Power and toughness update as graveyard card types change")
    void updatesWhenGraveyardCardTypesChange() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());

        harness.setGraveyard(player1, List.of(new Imperiosaur()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new JudgeUnworthy(), new JudgeUnworthy()));
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(2);
    }

    @Test
    @DisplayName("A card with multiple card types contributes each type")
    void countsAllTypesOnOneCard() {
        Permanent goyf = addCreatureReady(player1, new Tarmogoyf());
        harness.setGraveyard(player1, List.of(new DryadArbor()));

        assertThat(gqs.getEffectivePower(gd, goyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goyf)).isEqualTo(3);
    }
}
