package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({DamningVerdict.class, GrizzlyBears.class, HowlingMine.class})
class DamningVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures without counters and leaves countered creatures and noncreatures")
    void destroysOnlyCreaturesWithoutCounters() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.CHARGE, 1);
        harness.addToBattlefield(player1, new HowlingMine());

        castDamningVerdict();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertInGraveyard(player1, "Damning Verdict");
    }

    private void castDamningVerdict() {
        harness.castFromHand(player1, new DamningVerdict(), "{3}{W}{W}");
        harness.passBothPriorities();
    }
}
