package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.e.EerieProcession;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Subgoyf.class, Bonesplitter.class, EerieProcession.class, Forest.class,
        GrizzlyBears.class, JaceBeleren.class})
class SubgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Has 0/1 with empty graveyards")
    void hasBasePowerAndToughnessWithEmptyGraveyards() {
        Permanent subgoyf = addCreatureReady(player1, new Subgoyf());

        assertThat(gqs.getEffectivePower(gd, subgoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, subgoyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts distinct noncreature subtypes in all graveyards")
    void countsDistinctNonCreatureSubtypesInAllGraveyards() {
        Permanent subgoyf = addCreatureReady(player1, new Subgoyf());
        harness.setGraveyard(player1, List.of(
                new Forest(), new Bonesplitter(), new EerieProcession(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new JaceBeleren(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, subgoyf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, subgoyf)).isEqualTo(5);
    }

    @Test
    @DisplayName("Power and toughness update as graveyard subtypes change")
    void updatesWhenGraveyardSubtypesChange() {
        Permanent subgoyf = addCreatureReady(player1, new Subgoyf());

        harness.setGraveyard(player1, List.of(new Forest()));
        assertThat(gqs.getEffectivePower(gd, subgoyf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, subgoyf)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new Bonesplitter(), new EerieProcession()));
        assertThat(gqs.getEffectivePower(gd, subgoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, subgoyf)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, subgoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, subgoyf)).isEqualTo(3);
    }
}
