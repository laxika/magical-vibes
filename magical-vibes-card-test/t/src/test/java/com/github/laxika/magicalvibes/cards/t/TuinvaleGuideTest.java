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

    @Test
    @DisplayName("Entries before the Guide enters still enable celebration")
    void countsEntriesBeforeGuideEnters() {
        castGrizzlyBears();
        Permanent guide = castTuinvaleGuide();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isTrue();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's nonland entry does not enable your celebration")
    void doesNotCountOpponentsEntries() {
        Permanent guide = castTuinvaleGuide();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new TuinvaleGuide()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guide, Keyword.LIFELINK)).isFalse();
        Permanent opponentsGuide = findPermanent(player2, "Tuinvale Guide");
        assertThat(gqs.getEffectivePower(gd, opponentsGuide)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentsGuide, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Celebration grants lifelink for combat damage")
    void celebrationCombatDamageGainsLife() {
        Permanent guide = castTuinvaleGuide();
        castGrizzlyBears();
        guide.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
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
