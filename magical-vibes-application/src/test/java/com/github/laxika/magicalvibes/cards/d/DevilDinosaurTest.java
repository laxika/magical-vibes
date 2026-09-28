package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevilDinosaur.class, RaptorCompanion.class, GrizzlyBears.class})
class DevilDinosaurTest extends BaseCardTest {

    @Test
    void buffsOtherDinosaursAndGrantsThemHexproof() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        int basePower = gqs.getEffectivePower(gd, raptor);
        int baseToughness = gqs.getEffectiveToughness(gd, raptor);

        harness.addToBattlefield(player1, new DevilDinosaur());

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void doesNotAffectItselfNonDinosaursOrOpponents() {
        Permanent devil = harness.addToBattlefieldAndReturn(player1, new DevilDinosaur());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentRaptor = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        int bearPower = gqs.getEffectivePower(gd, bear);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);
        int opponentPower = gqs.getEffectivePower(gd, opponentRaptor);
        int opponentToughness = gqs.getEffectiveToughness(gd, opponentRaptor);

        assertThat(gqs.hasKeyword(gd, devil, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentRaptor)).isEqualTo(opponentPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentRaptor)).isEqualTo(opponentToughness);
        assertThat(gqs.hasKeyword(gd, opponentRaptor, Keyword.HEXPROOF)).isFalse();
    }
}
