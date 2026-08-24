package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanConstrictor.class, DrudgeSkeletons.class, GrizzlyBears.class})
class KrosanConstrictorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it gives a black creature -2/-0 until end of turn")
    void weakensTargetBlackCreatureUntilEndOfTurn() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent skeletons = addCreatureReady(player2, new DrudgeSkeletons());
        int originalPower = gqs.getEffectivePower(gd, skeletons);
        int originalToughness = gqs.getEffectiveToughness(gd, skeletons);

        harness.activateAbility(player1, 0, null, skeletons.getId());
        harness.passBothPriorities();

        assertThat(constrictor.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(originalPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(originalToughness);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        Permanent constrictor = addCreatureReady(player1, new KrosanConstrictor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(constrictor.isTapped()).isFalse();
    }
}
