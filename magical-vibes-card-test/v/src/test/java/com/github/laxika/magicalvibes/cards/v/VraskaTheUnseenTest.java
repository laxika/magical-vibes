package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.b.Brushstrider;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VraskaTheUnseen.class, DrudgeBeetle.class, Brushstrider.class, AxebaneGuardian.class, Forest.class})
class VraskaTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("+1 destroys a creature that deals combat damage to Vraska")
    void plusOneDestroysCreatureDealingCombatDamageToVraska() {
        Permanent vraska = addReadyVraska(5);
        Permanent attacker = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0), Map.of(0, vraska.getId()));
        resolveCombatAndTriggers(player2);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Drudge Beetle");
    }

    @Test
    @DisplayName("-3 destroys a target nonland permanent")
    void minusThreeDestroysNonlandPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent attacker = addCreatureReady(player2, new DrudgeBeetle());

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Drudge Beetle");
        harness.assertInGraveyard(player2, "Drudge Beetle");
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

        declareAttackers(player1, List.of(0));
        resolveCombatAndTriggers(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new VraskaTheUnseen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
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
        resolveCombat(activePlayer);
        resolveAllTriggers();
    }

    @Test
    void plusOneDoesNotDestroyBlockedAttackerThatOnlyDamagesItsBlocker() {
        Permanent vraska = addReadyVraska(5);
        Permanent attacker = addCreatureReady(player2, new Brushstrider());
        addCreatureReady(player1, new AxebaneGuardian());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0), Map.of(0, vraska.getId())));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombatAndTriggers(player2);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        harness.assertInGraveyard(player1, "Axebane Guardian");
    }

    @Test
    void plusOneStillDestroysAttackerWhenCombatDamageKillsVraska() {
        Permanent vraska = addReadyVraska(1);
        addCreatureReady(player2, new Brushstrider());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0), Map.of(0, vraska.getId()));
        resolveCombatAndTriggers(player2);

        harness.assertInGraveyard(player1, "Vraska the Unseen");
        harness.assertInGraveyard(player2, "Brushstrider");
    }

    @Test
    void minusThreeCannotTargetLand() {
        Permanent vraska = addReadyVraska(5);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void assassinCombatDamageToPlaneswalkerDoesNotMakeItsControllerLose() {
        addReadyVraska(7);
        Permanent opposingVraska = harness.addToBattlefieldAndReturn(player2, new VraskaTheUnseen());
        opposingVraska.setCounterCount(CounterType.LOYALTY, 5);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        declareAttackers(player1, List.of(0), Map.of(0, opposingVraska.getId()));
        resolveCombatAndTriggers(player1);

        assertThat(opposingVraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player2, 20);
    }
}
