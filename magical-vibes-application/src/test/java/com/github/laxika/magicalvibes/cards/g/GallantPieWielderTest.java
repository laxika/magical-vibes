package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({GallantPieWielder.class, Forest.class, GrizzlyBears.class})
class GallantPieWielderTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have double strike before celebration")
    void noDoubleStrikeBeforeCelebration() {
        Permanent pieWielder = castGallantPieWielder();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Has double strike after another nonland permanent enters under your control")
    void hasDoubleStrikeAfterNonlandPermanentEnters() {
        Permanent pieWielder = castGallantPieWielder();
        castGrizzlyBears();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent pieWielder = castGallantPieWielder();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent pieWielder = castGallantPieWielder();
        castGrizzlyBears();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent castGallantPieWielder() {
        harness.setHand(player1, List.of(new GallantPieWielder()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Gallant Pie-Wielder");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
