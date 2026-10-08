package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaravanEscort;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeneratedTeacher.class, CaravanEscort.class, Regress.class})
class VeneratedTeacherTest extends BaseCardTest {

    @Test
    @DisplayName("Its enter-the-battlefield ability puts level counters only on creatures with level up")
    void putsLevelCountersOnLevelUpCreatures() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent nonLeveler = harness.addToBattlefieldAndReturn(player1, new VeneratedTeacher());
        Permanent opposingEscort = harness.addToBattlefieldAndReturn(player2, new CaravanEscort());

        harness.setHand(player1, List.of(new VeneratedTeacher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
        assertThat(nonLeveler.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(opposingEscort.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void addsTwoCountersToEveryLevelerIncludingThoseAlreadyAtTheirHighestLevel() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        first.setCounterCount(CounterType.LEVEL, 5);
        second.setCounterCount(CounterType.LEVEL, 1);

        harness.setHand(player1, List.of(new VeneratedTeacher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.LEVEL)).isEqualTo(7);
        assertThat(second.getCounterCount(CounterType.LEVEL)).isEqualTo(3);
    }

    @Test
    void resolvesWithoutAnyLevelUpCreatures() {
        harness.setHand(player1, List.of(new VeneratedTeacher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Venerated Teacher");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.LEVEL)).isZero());
    }

    @Test
    void triggerStillResolvesAfterTeacherReturnsToHand() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        harness.setHand(player1, List.of(new VeneratedTeacher(), new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Venerated Teacher"));
        harness.assertNotOnBattlefield(player1, "Venerated Teacher");
        resolveAllTriggers();

        assertThat(escort.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
    }

    @Test
    void onlyAffectsLevelersStillOnBattlefieldWhenTriggerResolves() {
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new CaravanEscort());
        harness.setHand(player1, List.of(new VeneratedTeacher(), new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, escort.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Caravan Escort");
        assertThat(remaining.getCounterCount(CounterType.LEVEL)).isEqualTo(2);
    }
}
