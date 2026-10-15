package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkatewingSpy.class, AxebaneBeast.class})
class SkatewingSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts two +1/+1 counters on Skatewing Spy")
    void adaptPutsTwoCountersOnSkatewingSpy() {
        Permanent spy = addSpy();
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adapt can be activated once Skatewing Spy has a +1/+1 counter")
    void adaptCanBeActivatedWithCounter() {
        Permanent spy = addSpy();
        spy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have flying")
    void counteredOwnCreaturesHaveFlying() {
        Permanent spy = addSpy();
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());
        spy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, spy, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Skatewing Spy does not grant flying to uncountered or opposing creatures")
    void onlyCounteredOwnCreaturesHaveFlying() {
        addSpy();
        Permanent uncountered = addCreatureReady(player1, new AxebaneBeast());
        Permanent opponentCreature = addCreatureReady(player2, new AxebaneBeast());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Adapt checks for counters at resolution rather than activation")
    void adaptChecksCountersAtResolution() {
        Permanent spy = addSpy();
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        spy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(spy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adapt succeeds when the last counter is removed before resolution")
    void adaptSucceedsAfterLastCounterIsRemoved() {
        Permanent spy = addSpy();
        spy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, null);
        spy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(spy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spy, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying follows the presence of +1/+1 counters")
    void flyingTracksCounterChanges() {
        Permanent spy = addSpy();
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());
        creature.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, spy, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Countered creatures lose granted flying when Skatewing Spy leaves")
    void flyingEndsWhenSpyLeaves() {
        Permanent spy = addSpy();
        Permanent creature = addCreatureReady(player1, new AxebaneBeast());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(spy);
        gd.playerGraveyards.get(player1.getId()).add(spy.getCard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    private Permanent addSpy() {
        return addCreatureReady(player1, new SkatewingSpy());
    }
}
