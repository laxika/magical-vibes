package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SweettoothWitch;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GallantPieWielder.class, Forest.class, SweettoothWitch.class})
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
        castGallantPieWielder();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent pieWielder = castGallantPieWielder();
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent pieWielder = castGallantPieWielder();
        castGallantPieWielder();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent castGallantPieWielder() {
        harness.castFromHand(player1, new GallantPieWielder(), "{2}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Gallant Pie-Wielder");
    }

    @Test
    @DisplayName("Opponent's nonland entries do not enable celebration")
    void opposingEntriesDoNotCount() {
        Permanent pieWielder = castGallantPieWielder();
        harness.enterBattlefieldAndReturn(player2, new GallantPieWielder());
        harness.enterBattlefieldAndReturn(player2, new GallantPieWielder());

        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An existing Pie-Wielder gains double strike from this turn's entries")
    void entriesCountForAnExistingPieWielder() {
        Permanent pieWielder = harness.addToBattlefieldAndReturn(player1, new GallantPieWielder());

        harness.enterBattlefieldAndReturn(player1, new GallantPieWielder());
        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.enterBattlefieldAndReturn(player1, new GallantPieWielder());
        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Celebration still counts a permanent that left the battlefield")
    void departedPermanentStillCounts() {
        Permanent other = harness.enterBattlefieldAndReturn(player1, new GallantPieWielder());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, other));

        Permanent pieWielder = castGallantPieWielder();

        harness.assertInGraveyard(player1, "Gallant Pie-Wielder");
        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A Food token counts as the second nonland permanent")
    void foodTokenEnablesCelebration() {
        Permanent pieWielder = harness.addToBattlefieldAndReturn(player1, new GallantPieWielder());

        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Food");
        assertThat(gqs.hasKeyword(gd, pieWielder, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
