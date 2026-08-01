package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VraskaTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("+1 destroys a creature that deals combat damage to Vraska")
    void plusOneDestroysCreatureDealingCombatDamageToVraska() {
        Permanent vraska = addReadyVraska(5);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0), Map.of(0, vraska.getId()));
        resolveCombatAndTriggers(player2);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-3 destroys a target nonland permanent")
    void minusThreeDestroysNonlandPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-7 creates Assassin tokens whose combat damage makes a player lose")
    void minusSevenCreatesLosingAssassinTokens() {
        addReadyVraska(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        List<Permanent> assassins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().hasType(CardType.CREATURE))
                .toList();
        assertThat(assassins).hasSize(3);
        assassins.forEach(token -> token.setSummoningSick(false));

        declareAttackers(player1, List.of(0), null);
        resolveCombatAndTriggers(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent perm = new Permanent(new VraskaTheUnseen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> indices, Map<Integer, java.util.UUID> targets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, indices, targets);
    }

    private void resolveCombatAndTriggers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
