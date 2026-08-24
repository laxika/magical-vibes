package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent vraska = new Permanent(new VraskaSwarmsEminence());
        vraska.setCounterCount(CounterType.LOYALTY, loyalty);
        vraska.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(vraska);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return vraska;
    }

    private Permanent addReadyDeathtouchPyromancer() {
        Card card = new ProdigalPyromancer();
        card.setKeywords(Set.of(Keyword.DEATHTOUCH));
        Permanent pyromancer = new Permanent(card);
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);
        return pyromancer;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
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
