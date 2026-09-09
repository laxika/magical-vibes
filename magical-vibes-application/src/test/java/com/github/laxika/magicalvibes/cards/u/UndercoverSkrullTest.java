package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercoverSkrull.class, GrizzlyBears.class, Forest.class})
class UndercoverSkrullTest extends BaseCardTest {

    @Test
    void getsBoostAndAllCreatureTypesWithTwoCreatureCardsInGraveyard() {
        Permanent skrull = addReadySkrull();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skrull)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ZOMBIE)).isTrue();
    }

    @Test
    void doesNotCountNoncreatureOrOpponentCardsForThreshold() {
        Permanent skrull = addReadySkrull();
        int basePower = gqs.getEffectivePower(gd, skrull);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(basePower);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isFalse();

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, skrull)).isEqualTo(basePower);
        assertThat(gqs.hasEffectiveSubtype(gd, skrull, CardSubtype.ELF)).isFalse();
    }

    @Test
    void tapsForOneManaOfEachChosenColor() {
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness = new com.github.laxika.magicalvibes.testutil.GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            Permanent skrull = harness.addToBattlefieldAndReturn(player1, new UndercoverSkrull());
            skrull.setSummoningSick(false);
            harness.activateAbility(player1, 0, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(color))
                    .isEqualTo(1);
        }
    }

    private Permanent addReadySkrull() {
        Permanent skrull = harness.addToBattlefieldAndReturn(player1, new UndercoverSkrull());
        skrull.setSummoningSick(false);
        return skrull;
    }
}
