package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KappaCannoneer.class, GlazeFiend.class})
class KappaCannoneerTest extends BaseCardTest {

    @Test
    @DisplayName("Kappa Cannoneer gets a counter and can't be blocked when it enters")
    void selfEntryTriggers() {
        harness.setHand(player1, List.of(new KappaCannoneer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent kappa = findPermanent(player1, "Kappa Cannoneer");
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
    }

    @Test
    @DisplayName("Another artifact entering gives Kappa Cannoneer a counter and unblockability")
    void anotherArtifactEntryTriggers() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();
    }

    @Test
    @DisplayName("Kappa Cannoneer's temporary unblockability wears off at cleanup")
    void unblockabilityWearsOffAtCleanup() {
        Permanent kappa = harness.addToBattlefieldAndReturn(player1, new KappaCannoneer());

        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, kappa)).isFalse();
        assertThat(kappa.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
