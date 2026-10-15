package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfHours.class, GiantGrowth.class, Shock.class, Hubris.class})
class SageOfHoursTest extends BaseCardTest {

    @Test
    @DisplayName("Casting your spell that targets Sage of Hours puts a +1/+1 counter on it")
    void ownTargetingSpellTriggersHeroic() {
        harness.addToBattlefield(player1, new SageOfHours());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID sageId = harness.getPermanentId(player1, "Sage of Hours");
        harness.castAndResolveInstant(player1, 0, sageId);
        harness.passBothPriorities();

        Permanent sage = findPermanent(player1, "Sage of Hours");
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger Sage of Hours")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SageOfHours());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent sage = findPermanent(player1, "Sage of Hours");
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell targeting Sage of Hours does not trigger heroic")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SageOfHours());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID sageId = harness.getPermanentId(player1, "Sage of Hours");
        harness.castAndResolveInstant(player2, 0, sageId);

        Permanent sage = findPermanent(player1, "Sage of Hours");
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Removing ten +1/+1 counters queues two extra turns")
    void removesCountersForExtraTurns() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId(), player1.getId());
    }

    @Test
    @DisplayName("Removing fewer than five counters still removes all counters but grants no extra turn")
    void removesAllCountersBelowThreshold() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated without counters and grants no extra turn")
    void activatesWithNoCounters() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Seven counters grant one extra turn and leave other counter types intact")
    void roundsDownAndRemovesOnlyPlusOneCounters() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        sage.setCounterCount(CounterType.CHARGE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sage.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Extra turns use the paid counter count even if Sage leaves the battlefield")
    void extraTurnAbilitySurvivesSourceLeaving() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, sage.getId());
        harness.assertNotOnBattlefield(player1, "Sage of Hours");
        harness.assertInHand(player1, "Sage of Hours");
        assertThat(gd.extraTurns).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Heroic resolves before the spell and can add counters after the activation cost")
    void newHeroicCounterDoesNotChangePaidCounterCount() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfHours());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player1, 0, sage.getId());
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.extraTurns).isEmpty();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }
}
