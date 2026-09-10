package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StaunchDefenders.class)
class StaunchDefendersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield triggers a life-gain ability")
    void entryTriggersLifeGain() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
    }

    @Test
    @DisplayName("Resolving the ETB trigger gains 4 life")
    void entryGainsFourLife() {
        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("ETB life gain affects only the creature's controller")
    void entryGainsLifeForControllerOnly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 17);

        harness.castFromHand(player1, new StaunchDefenders(), "{3}{W}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 17);
    }
}
