package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GraniteShard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarElemental.class, GraniteShard.class, AlphaMyr.class})
class WarElementalTest extends BaseCardTest {

    @Test
    void sacrificesOnEntryIfNoOpponentWasDealtDamageThisTurn() {
        harness.setHand(player1, List.of(new WarElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "War Elemental");
    }

    @Test
    void survivesEntryIfOpponentWasDealtDamageEarlierThisTurn() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setHand(player1, List.of(new WarElemental()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shard),
                1, null, player2.getId());
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "War Elemental");
    }

    @Test
    void survivesIfOpponentIsDealtDamageBeforeEntryAbilityResolves() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.setHand(player1, List.of(new WarElemental()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "War Elemental");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shard),
                1, null, player2.getId());
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "War Elemental");
    }

    @Test
    void getsThatManyCountersWhenOpponentIsDealtDamage() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WarElemental());
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(shard),
                1, null, player2.getId());
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForDamageToControllerOrCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WarElemental());
        Permanent controllerDamageSource = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        Permanent creatureDamageSource = harness.addToBattlefieldAndReturn(player1, new GraniteShard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(controllerDamageSource),
                1, null, player1.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(creatureDamageSource),
                1, null, creature.getId());
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void getsThatManyCountersForCombatDamageToOpponent() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WarElemental());
        Permanent attacker = addCreatureReady(player1, new AlphaMyr());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
