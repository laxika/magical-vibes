package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillarLaunch.class, GrizzlyBears.class, FountainOfYouth.class})
class PillarLaunchTest extends BaseCardTest {

    @Test
    @DisplayName("Pillar Launch boosts, grants reach to, and untaps the target creature")
    void boostsGrantsReachAndUntapsTarget() {
        Permanent target = addTappedCreature();
        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).contains(Keyword.REACH);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Pillar Launch's boost and reach wear off at cleanup")
    void temporaryEffectsWearOffAtCleanup() {
        Permanent target = addTappedCreature();
        cast(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.REACH);
    }

    @Test
    @DisplayName("Pillar Launch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new PillarLaunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent target = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);
        target.tap();
        return target;
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new PillarLaunch()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
