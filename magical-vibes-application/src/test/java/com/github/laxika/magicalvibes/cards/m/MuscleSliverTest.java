package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmorSliver;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuscleSliver.class, ArmorSliver.class, TrainedArmodon.class})
class MuscleSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Muscle Sliver boosts itself (it is a Sliver)")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new ArmorSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Boosts an opponent's Sliver too (all Slivers)")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new ArmorSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new MuscleSliver());
        Permanent creature = addCreatureReady(player1, new TrainedArmodon());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Multiple Muscle Slivers stack their boosts")
    void multipleMuscleSliversStack() {
        Permanent first = addCreatureReady(player1, new MuscleSliver());
        Permanent second = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Stops boosting Slivers when Muscle Sliver leaves the battlefield")
    void stopsBoostingWhenSourceLeavesBattlefield() {
        Permanent otherSliver = addCreatureReady(player1, new ArmorSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);
        Permanent source = addCreatureReady(player1, new MuscleSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, source));

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness);
    }
}
