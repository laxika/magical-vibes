package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HyalopterousLemureTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives -1/-0 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(3); // 4 - 1
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent lemure = harness.addToBattlefieldAndReturn(player1, new HyalopterousLemure());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lemure)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lemure)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lemure, Keyword.FLYING)).isFalse();
    }
}
