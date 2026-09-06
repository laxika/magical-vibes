package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TuinvaleGuide.class, Forest.class, GrizzlyBears.class})
class TuinvaleGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 and lifelink after two nonland permanents enter under your control this turn")
    void getsCelebrationBonusAfterTwoNonlandPermanentsEnter() {
        Permanent guide = castTuinvaleGuide();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isFalse();

        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent guide = castTuinvaleGuide();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent guide = castTuinvaleGuide();
        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isFalse();
    }

    private Permanent castTuinvaleGuide() {
        harness.setHand(player1, List.of(new TuinvaleGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Tuinvale Guide");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
