package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowstoneSurge.class, FlowstoneCrusher.class, Opalescence.class})
class FlowstoneSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures get +1/-1")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new FlowstoneSurge());
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new FlowstoneCrusher());

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not affect opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new FlowstoneSurge());
        Permanent crusher = harness.addToBattlefieldAndReturn(player2, new FlowstoneCrusher());

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus starts on resolution and applies to creatures entering later")
    void bonusStartsOnResolutionAndAffectsLaterCreatures() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new FlowstoneCrusher());
        harness.castFromHand(player1, new FlowstoneSurge(), "{1}{R}");

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(3);
        Permanent laterCrusher = harness.enterBattlefieldAndReturn(player1, new FlowstoneCrusher());
        assertThat(gqs.getEffectivePower(gd, laterCrusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, laterCrusher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple copies stack and put a creature with zero toughness into its graveyard")
    void stackedCopiesCauseZeroToughnessDeath() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new FlowstoneCrusher());
        harness.addToBattlefield(player2, new FlowstoneCrusher());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new FlowstoneSurge());
        }

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isZero();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Flowstone Crusher");
        harness.assertInGraveyard(player1, "Flowstone Crusher");
        harness.assertOnBattlefield(player2, "Flowstone Crusher");
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Boosts itself when Opalescence makes it a creature")
    void boostsItselfWhenItBecomesACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent surge = harness.addToBattlefieldAndReturn(player1, new FlowstoneSurge());

        assertThat(gqs.isCreature(gd, surge)).isTrue();
        assertThat(gqs.getEffectivePower(gd, surge)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, surge)).isEqualTo(1);
    }
}
