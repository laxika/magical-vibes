package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandBallGuest.class, Forest.class})
class GrandBallGuestTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and trample after two nonland permanents enter under your control this turn")
    void getsCelebrationBonusAfterTwoNonlandPermanentsEnter() {
        Permanent guest = castGrandBallGuest();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();

        castGrandBallGuest();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent guest = castGrandBallGuest();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent guest = castGrandBallGuest();
        castGrandBallGuest();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's entries do not enable celebration")
    void doesNotCountOpponentsPermanents() {
        Permanent guest = castGrandBallGuest();
        harness.enterBattlefieldAndReturn(player2, new GrandBallGuest());
        harness.enterBattlefieldAndReturn(player2, new GrandBallGuest());

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Entries before the Guest arrived count even after they leave")
    void countsEarlierEntriesAfterTheyLeave() {
        Permanent earlier = harness.enterBattlefieldAndReturn(player1, new GrandBallGuest());
        gd.playerBattlefields.get(player1.getId()).remove(earlier);

        Permanent guest = castGrandBallGuest();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Celebration does not stack after more than two entries")
    void bonusDoesNotStack() {
        Permanent guest = castGrandBallGuest();
        harness.enterBattlefieldAndReturn(player1, new GrandBallGuest());
        harness.enterBattlefieldAndReturn(player1, new GrandBallGuest());

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent castGrandBallGuest() {
        harness.castFromHand(player1, new GrandBallGuest(), "{1}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Grand Ball Guest");
    }

}
