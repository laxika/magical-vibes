package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AgentOfAtlas;
import com.github.laxika.magicalvibes.cards.a.AerialDoombot;
import com.github.laxika.magicalvibes.cards.h.HumanTorchJohnnyStorm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThingBenGrimm.class, AgentOfAtlas.class, AerialDoombot.class, HumanTorchJohnnyStorm.class})
class TheThingBenGrimmTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setAttacking(true);
        return permanent;
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on itself when a Hero deals damage to a player")
    void heroDamagePutsTwoCountersOnTheThing() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AgentOfAtlas());

        resolveCombat();

        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers only once when multiple Heroes deal damage in the same event")
    void multipleHeroesTriggerOnlyOnce() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AgentOfAtlas());
        addAttacker(new AgentOfAtlas());

        resolveCombat();

        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when a non-Hero creature deals damage to a player")
    void nonHeroDamageDoesNotTrigger() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AerialDoombot());

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The Thing counts its own damage to a player")
    void ownDamageTriggers() {
        Permanent theThing = addAttacker(new TheThingBenGrimm());

        resolveCombat();
        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("Mixed Hero and non-Hero damage still triggers once")
    void mixedDamageTriggersOnce() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addAttacker(new AerialDoombot());
        addAttacker(new AgentOfAtlas());

        resolveCombat();
        resolveAllTriggers();

        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent's Hero dealing damage does not trigger The Thing")
    void opposingHeroDoesNotTrigger() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        Permanent hero = addCreatureReady(player2, new AgentOfAtlas());
        hero.setAttacking(true);
        theThing.tap();

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Noncombat damage from a Hero also puts two counters on The Thing")
    void noncombatHeroDamageTriggers() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        harness.addToBattlefield(player1, new HumanTorchJohnnyStorm());
        harness.setLibrary(player1, List.of(new AgentOfAtlas()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample damage to a player triggers counters after damage is dealt")
    void trampleDamageTriggers() {
        Permanent theThing = addCreatureReady(player1, new TheThingBenGrimm());
        Permanent blocker = addCreatureReady(player2, new AerialDoombot());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 6));
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player2, "Aerial Doombot");
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hero damage dealt only to a creature does not trigger counters")
    void damageToCreatureDoesNotTrigger() {
        Permanent theThing = harness.addToBattlefieldAndReturn(player1, new TheThingBenGrimm());
        addCreatureReady(player1, new AgentOfAtlas());
        addCreatureReady(player2, new AerialDoombot());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Aerial Doombot");
        assertThat(theThing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
