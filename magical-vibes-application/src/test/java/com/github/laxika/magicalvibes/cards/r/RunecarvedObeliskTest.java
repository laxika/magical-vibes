package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunecarvedObelisk.class, CommandersSphere.class, FountainOfYouth.class, GrizzlyBears.class})
class RunecarvedObeliskTest extends BaseCardTest {

    @Test
    void tappingAddsTwoColorlessManaAndTwoChargeCounters() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new RunecarvedObelisk());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(obelisk.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(obelisk.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndSeeksTheHighestManaValueAtOrBelowItsChargeCounters() {
        Permanent obelisk = harness.addToBattlefieldAndReturn(player1, new RunecarvedObelisk());
        obelisk.setCounterCount(CounterType.CHARGE, 2);

        Card tooExpensive = new CommandersSphere();
        Card highestAllowed = new GrizzlyBears();
        Card lower = new FountainOfYouth();
        harness.setLibrary(player1, List.of(tooExpensive, highestAllowed, lower));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Runecarved Obelisk");
        harness.assertInGraveyard(player1, "Runecarved Obelisk");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tooExpensive, lower);
    }
}
