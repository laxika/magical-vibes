package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VraskaGolgariQueenTest extends BaseCardTest {

    @Test
    @DisplayName("+2 sacrifices another permanent, gains life, and draws a card")
    void plusTwoSacrificesGainsLifeAndDraws() {
        Permanent vraska = addReadyVraska(5);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("+2 may be declined")
    void plusTwoMayBeDeclined() {
        Permanent vraska = addReadyVraska(5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("+2 only offers another permanent to sacrifice")
    void plusTwoOnlyOffersAnotherPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(vraska.getId());
    }

    @Test
    @DisplayName("-3 destroys a nonland permanent with mana value 3 or less")
    void minusThreeDestroysSmallNonlandPermanent() {
        Permanent vraska = addReadyVraska(5);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, 1, null, elves.getId());
        harness.passBothPriorities();

        assertThat(vraska.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("-3 rejects a land target")
    void minusThreeRejectsLand() {
        addReadyVraska(5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-9 emblem makes a player lose when a controlled creature deals combat damage")
    void minusNineEmblemMakesPlayerLoseFromCombatDamage() {
        addReadyVraska(9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        declareAttack(player1, bears, player2.getId());
        resolveCombatAndTriggers(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("-9 emblem does not trigger from an opponent's creature")
    void minusNineEmblemDoesNotTriggerFromOpponentsCreature() {
        addReadyVraska(9);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        declareAttack(player2, bears, player1.getId());
        resolveCombatAndTriggers(player2);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addReadyVraska(int loyalty) {
        Permanent perm = new Permanent(new VraskaGolgariQueen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void declareAttack(Player attackingPlayer, Permanent attacker, java.util.UUID defenderId) {
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int index = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackingPlayer, List.of(index), Map.of(index, defenderId));
    }

    private void resolveCombatAndTriggers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
