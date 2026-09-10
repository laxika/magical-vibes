package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AgentOfAtlas;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThingBenGrimm.class, AgentOfAtlas.class, GrizzlyBears.class})
class TheThingBenGrimmTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on itself when a Hero deals damage to a player")
    void heroDamagePutsTwoCountersOnTheThing() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AgentOfAtlas());

        runCombatDamage();

        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers only once when multiple Heroes deal damage in the same event")
    void multipleHeroesTriggerOnlyOnce() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AgentOfAtlas());
        addAttacker(new AgentOfAtlas());

        runCombatDamage();

        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Hero creature deals damage to a player")
    void nonHeroDamageDoesNotTrigger() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new GrizzlyBears());

        runCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
