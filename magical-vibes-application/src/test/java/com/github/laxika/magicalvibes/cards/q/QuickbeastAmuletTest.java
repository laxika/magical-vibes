package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuickbeastAmulet.class, GrizzlyBears.class, HillGiant.class})
class QuickbeastAmuletTest extends BaseCardTest {

    @Test
    void intensifiesByEnteringCreaturePowerAndBoostsEquippedCreature() {
        harness.addToBattlefield(player1, new QuickbeastAmulet());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent amulet = findPermanent(player1, "Quickbeast Amulet");
        amulet.setAttachedTo(bears.getId());

        assertThat(amulet.getCounterCount(CounterType.INTENSITY)).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(amulet.getCounterCount(CounterType.INTENSITY)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    void doesNotIntensifyForAnOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new QuickbeastAmulet());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Quickbeast Amulet")
                .getCounterCount(CounterType.INTENSITY)).isZero();
    }
}
