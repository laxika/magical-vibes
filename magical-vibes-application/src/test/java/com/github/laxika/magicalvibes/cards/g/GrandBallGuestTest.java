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

@CardUsed({GrandBallGuest.class, Forest.class, GrizzlyBears.class})
class GrandBallGuestTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and trample after two nonland permanents enter under your control this turn")
    void getsCelebrationBonusAfterTwoNonlandPermanentsEnter() {
        Permanent guest = castGrandBallGuest();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();

        castGrizzlyBears();

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent guest = castGrandBallGuest();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        assertThat(gqs.getEffectivePower(gd, guest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guest)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, guest, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Celebration ends when the turn changes")
    void celebrationEndsAtTurnChange() {
        Permanent guest = castGrandBallGuest();
        castGrizzlyBears();

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

    private Permanent castGrandBallGuest() {
        harness.setHand(player1, List.of(new GrandBallGuest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Grand Ball Guest");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
