package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoodFortuneUnicorn.class, GrizzlyBears.class, LlanowarElves.class,
        DwynensElite.class, Unsummon.class, PincherBeetles.class})
class GoodFortuneUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering under your control gets a +1/+1 counter")
    void anotherAllyCreatureEnteringGetsCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature entering does not get a counter")
    void opponentCreatureEnteringDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent enteringCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Good-Fortune Unicorn's own entry does not trigger its ability")
    void ownEntryDoesNotTrigger() {
        harness.castFromHand(player1, new GoodFortuneUnicorn(), "{1}{G}{W}");
        harness.passBothPriorities();

        Permanent unicorn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Unicorn gives another entering creature a counter")
    void multipleUnicornsEachGiveCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoodFortuneUnicorn());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GoodFortuneUnicorn());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An entering Unicorn gets a counter from an existing Unicorn only")
    void enteringUnicornTriggersExistingUnicorn() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GoodFortuneUnicorn());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GoodFortuneUnicorn());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter trigger still resolves after the Unicorn leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new GoodFortuneUnicorn());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, unicorn.getId());
        harness.assertNotOnBattlefield(player1, "Good-Fortune Unicorn");
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature that leaves before the trigger resolves receives no counter")
    void enteringCreatureLeavesBeforeResolution() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new GoodFortuneUnicorn());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, entering.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(unicorn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature tokens also receive a counter")
    void creatureTokenEnteringGetsCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.castFromHand(player1, new DwynensElite(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud does not prevent the entering creature from receiving a counter")
    void shroudDoesNotPreventCounter() {
        harness.addToBattlefield(player1, new GoodFortuneUnicorn());
        harness.castFromHand(player1, new PincherBeetles(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent entering = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
