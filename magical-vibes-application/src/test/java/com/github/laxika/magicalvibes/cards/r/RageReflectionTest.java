package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageReflection.class, BriarberryCohort.class})
class RageReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's creatures have double strike")
    void controllersCreaturesHaveDoubleStrike() {
        harness.addToBattlefield(player1, new RageReflection());
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do NOT have double strike")
    void opponentsCreaturesDoNotHaveDoubleStrike() {
        harness.addToBattlefield(player1, new RageReflection());
        Permanent cohort = harness.addToBattlefieldAndReturn(player2, new BriarberryCohort());

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creatures lose double strike when Rage Reflection leaves the battlefield")
    void doubleStrikeLostWhenRageReflectionLeaves() {
        Permanent reflection = harness.addToBattlefieldAndReturn(player1, new RageReflection());
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(reflection);

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
