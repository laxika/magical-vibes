package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RageReflection.class, BriarberryCohort.class, Opalescence.class})
class RageReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Rage Reflection grants double strike to creatures already on the battlefield")
    void grantsDoubleStrikeToExistingCreaturesOnResolution() {
        Permanent cohort = harness.addToBattlefieldAndReturn(player1, new BriarberryCohort());

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.castFromHand(player1, new RageReflection(), "{4}{R}{R}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cohort, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An unblocked creature deals damage in both combat damage steps")
    void unblockedCreatureDealsDamageTwice() {
        harness.addToBattlefield(player1, new RageReflection());
        addCreatureReady(player1, new BriarberryCohort());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({RageReflection.class, Opalescence.class})
    @DisplayName("Rage Reflection grants itself double strike when it becomes a creature")
    void animatedRageReflectionHasDoubleStrike() {
        Permanent reflection = harness.addToBattlefieldAndReturn(player1, new RageReflection());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, reflection)).isTrue();
        assertThat(gqs.hasKeyword(gd, reflection, Keyword.DOUBLE_STRIKE)).isTrue();
    }

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
