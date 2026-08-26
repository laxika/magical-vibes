package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LashOfThorns.class, GrizzlyBears.class, Forest.class})
class LashOfThornsTest extends BaseCardTest {

    @Test
    void boostsAndGrantsDeathtouch() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOn(bear);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void effectsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOn(bear);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LashOfThorns()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new LashOfThorns()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
