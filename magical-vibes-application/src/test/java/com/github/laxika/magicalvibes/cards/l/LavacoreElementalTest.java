package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.NeedlepeakSpider;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.t.Timecrafting;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavacoreElemental.class, NeedlepeakSpider.class, ProdigalPyromancer.class, Timecrafting.class})
class LavacoreElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one time counter")
    void entersWithTimeCounter() {
        harness.setHand(player1, List.of(new LavacoreElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent elemental = findPermanent(player1, "Lavacore Elemental");

        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a time counter when a creature you control deals combat damage to a player")
    void getsTimeCounterOnAllyCombatDamage() {
        Permanent elemental = addReadyElemental();
        Permanent spider = addCreatureReady(player1, new NeedlepeakSpider());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(spider)));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes one time counter and stays on the battlefield when more remain")
    void removesOneTimeCounterWhenMoreRemain() {
        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Does not get a time counter from an opponent's creature dealing combat damage")
    void doesNotGetTimeCounterFromOpponentsCombatDamage() {
        Permanent elemental = addReadyElemental();
        Permanent spider = addCreatureReady(player2, new NeedlepeakSpider());

        declareAttackersAndPrepareBlockers(player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(spider)));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a time counter when combat damage is dealt only to a creature")
    void doesNotGetTimeCounterFromCombatDamageToCreature() {
        Permanent elemental = addReadyElemental();
        Permanent attacker = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent blocker = addCreatureReady(player2, new NeedlepeakSpider());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a time counter from noncombat damage to a player")
    void doesNotGetTimeCounterFromNoncombatDamage() {
        Permanent elemental = addReadyElemental();
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        int pyromancerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer);
        harness.activateAbility(player1, pyromancerIndex, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed during upkeep")
    void sacrificesOnLastTimeCounter() {
        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lavacore Elemental");
        harness.assertInGraveyard(player1, "Lavacore Elemental");
    }

    @Test
    void getsTimeCounterFromItsOwnCombatDamage() {
        Permanent elemental = addReadyElemental();

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(elemental)));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void getsOneTimeCounterForEachCreatureDealingCombatDamage() {
        Permanent elemental = addReadyElemental();
        Permanent first = addCreatureReady(player1, new NeedlepeakSpider());
        Permanent second = addCreatureReady(player1, new NeedlepeakSpider());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(3);
    }

    @Test
    void sacrificesWhenTimecraftingRemovesLastTimeCounter() {
        Permanent elemental = addReadyElemental();
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstantForX(player1, 0, 0, 1, elemental.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lavacore Elemental");
        harness.assertInGraveyard(player1, "Lavacore Elemental");
    }

    @Test
    void doesNotTriggerUpkeepWithoutTimeCounters() {
        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.TIME, 0);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    private Permanent addReadyElemental() {
        Permanent permanent = addCreatureReady(player1, new LavacoreElemental());
        permanent.setCounterCount(CounterType.TIME, 1);
        return permanent;
    }
}
