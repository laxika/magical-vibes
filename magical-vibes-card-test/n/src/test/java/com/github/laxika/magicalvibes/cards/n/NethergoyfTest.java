package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nethergoyf.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class, Ornithopter.class})
class NethergoyfTest extends BaseCardTest {

    @Test
    void powerAndToughnessCountDistinctCardTypesInYourGraveyard() {
        Permanent nethergoyf = addCreatureReady(player1, new Nethergoyf());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, nethergoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nethergoyf)).isEqualTo(4);
    }

    @Test
    void escapesByExilingFourOtherCards() {
        Nethergoyf nethergoyf = new Nethergoyf();
        Forest first = new Forest();
        Shock second = new Shock();
        Millstone third = new Millstone();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nethergoyf, first, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        resolveAllTriggers();

        Permanent escaped = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(escaped.getCard()).isInstanceOf(Nethergoyf.class);
        assertThat(escaped.isEscaped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(escaped.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);
    }

    @Test
    void escapeRequiresFourTypesAmongExiledCards() {
        harness.setGraveyard(player1, List.of(new Nethergoyf(), new Forest(), new Shock(), new Millstone()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapeRejectsFourCardsWithOnlyOneType() {
        harness.setGraveyard(player1, List.of(new Nethergoyf(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Shock(), new Millstone(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapesWithThreeCardsContainingFourTypes() {
        Nethergoyf nethergoyf = new Nethergoyf();
        Forest land = new Forest();
        Shock instant = new Shock();
        Ornithopter artifactCreature = new Ornithopter();
        harness.setGraveyard(player1, List.of(nethergoyf, land, instant, artifactCreature));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        resolveAllTriggers();

        Permanent escaped = findPermanent(player1, "Nethergoyf");
        assertThat(escaped.isEscaped()).isTrue();
        assertThat(gd.getCardsExiledByPermanent(escaped.getId()))
                .containsExactlyInAnyOrder(land, instant, artifactCreature);
        assertThat(gqs.getEffectivePower(gd, escaped)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, escaped)).isEqualTo(1);
    }

    @Test
    void escapesWithMoreThanFourCardsContainingFourTypes() {
        harness.setGraveyard(player1, List.of(new Nethergoyf(), new Forest(), new Shock(),
                new Millstone(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4, 5));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Nethergoyf").isEscaped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void powerAndToughnessCountMultipleTypesOnOneCardAndIgnoreDuplicates() {
        Permanent nethergoyf = addCreatureReady(player1, new Nethergoyf());
        harness.setGraveyard(player1, List.of(new Ornithopter(), new Millstone(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, nethergoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nethergoyf)).isEqualTo(3);

        harness.setGraveyard(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, nethergoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, nethergoyf)).isEqualTo(1);
    }

    @Test
    void powerAndToughnessAreDefinedInTheGraveyardIncludingItsOwnCreatureType() {
        Nethergoyf nethergoyf = new Nethergoyf();
        harness.setGraveyard(player1, List.of(nethergoyf, new Forest(), new Millstone()));
        harness.setGraveyard(player2, List.of(new Shock()));

        assertThat(gqs.getEffectiveCardPower(gd, nethergoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, nethergoyf)).isEqualTo(4);
    }
}
