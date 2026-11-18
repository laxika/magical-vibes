package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncheckedGrowth.class, GoblinCohort.class, TeardropKami.class})
class UncheckedGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a non-Spirit creature +4/+4 without trample")
    void boostsNonSpiritWithoutTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoblinCohort());
        castUncheckedGrowth(target.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Gives a Spirit creature +4/+4 and trample")
    void boostsSpiritWithTrample() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        castUncheckedGrowth(target.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeardropKami());
        castUncheckedGrowth(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void boostsOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());
        castUncheckedGrowth(target.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Grants trample to an opponent's Spirit without affecting other creatures")
    void boostsOnlyTargetedOpponentsSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TeardropKami());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TeardropKami());

        castUncheckedGrowth(target.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Unchecked Growth boosts accumulate and expire together")
    void repeatedBoostsAccumulateUntilCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TeardropKami());

        castUncheckedGrowth(target.getId());
        castUncheckedGrowth(target.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(8);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    private void castUncheckedGrowth(UUID targetId) {
        harness.setHand(player1, List.of(new UncheckedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
