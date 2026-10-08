package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VraskaSwarmsEminence.class, ProdigalPyromancer.class})
class VraskaSwarmsEminenceTest extends BaseCardTest {

    @Test
    void deathtouchCreatureDamageToPlayerGetsCounterOnThatCreature() {
        Permanent vraska = addReadyVraska(5);
        Permanent pyromancer = addReadyDeathtouchPyromancer();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(pyromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void deathtouchCreatureDamageToPlaneswalkerGetsCounterOnThatCreature() {
        Permanent vraska = addReadyVraska(5);
        Permanent pyromancer = addReadyDeathtouchPyromancer();
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(pyromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void assassinDestroysPlaneswalkerItDamages() {
        Permanent vraska = addReadyVraska(5);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent assassin = findPermanents(player1, "Assassin").getFirst();
        assassin.setSummoningSick(false);

        declareAttack(player1, assassin, planeswalker.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void deathtouchCreatureDamageToItsControllerGetsCounter() {
        addReadyVraska(5);
        Permanent pyromancer = addReadyDeathtouchPyromancer();

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(pyromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureWithoutDeathtouchDoesNotGetCounter() {
        addReadyVraska(5);
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(pyromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void deathtouchCreatureDamageToOwnPlaneswalkerGetsCounter() {
        Permanent vraska = addReadyVraska(5);
        Permanent pyromancer = addReadyDeathtouchPyromancer();

        harness.activateAbility(player1, 1, null, vraska.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(pyromancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void assassinStillDestroysPlaneswalkerAfterVraskaLeaves() {
        Permanent vraska = addReadyVraska(2);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent assassin = findPermanent(player1, "Assassin");
        assassin.setSummoningSick(false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vraska);
        declareAttack(player1, assassin, planeswalker.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent vraska = harness.addToBattlefieldAndReturn(player1, new VraskaSwarmsEminence());
        vraska.setCounterCount(CounterType.LOYALTY, loyalty);
        vraska.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return vraska;
    }

    private Permanent addReadyDeathtouchPyromancer() {
        Card card = new ProdigalPyromancer();
        card.setKeywords(Set.of(Keyword.DEATHTOUCH));
        return addCreatureReady(player1, card);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new VraskaSwarmsEminence());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }

    private void declareAttack(Player attackingPlayer, Permanent attacker, UUID targetId) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackingPlayer, List.of(attackerIndex), Map.of(attackerIndex, targetId));
    }
}
