package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DamningVerdict.class, FountainOfYouth.class, GrizzlyBears.class})
class DamningVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only creatures without counters")
    void destroysOnlyCreaturesWithoutCounters() {
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.castFromHand(player1, new DamningVerdict(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(uncounteredCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(counteredCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(noncreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
