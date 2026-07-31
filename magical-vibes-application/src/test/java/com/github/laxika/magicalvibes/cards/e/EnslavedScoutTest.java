package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnslavedScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Gains mountainwalk until end of turn")
    void gainsMountainwalk() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        scout.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();
    }
}
