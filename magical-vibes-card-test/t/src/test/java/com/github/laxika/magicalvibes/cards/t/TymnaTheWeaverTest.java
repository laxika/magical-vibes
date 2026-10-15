package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TymnaTheWeaver.class, GrizzlyBears.class})
class TymnaTheWeaverTest extends BaseCardTest {

    @Test
    void paysLifeAndDrawsForEachDistinctOpponentDealtCombatDamage() {
        addCreatureReady(player1, new TymnaTheWeaver());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        markCombatDamage(firstAttacker, player2);
        markCombatDamage(secondAttacker, player2);

        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void decliningPaymentDoesNotDrawOrLoseLife() {
        addCreatureReady(player1, new TymnaTheWeaver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        markCombatDamage(attacker, player2);

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotPromptWhenNoOpponentWasDealtCombatDamage() {
        addCreatureReady(player1, new TymnaTheWeaver());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsPostcombatMain() {
        addCreatureReady(player1, new TymnaTheWeaver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        markCombatDamage(attacker, player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageToControllerDoesNotCount() {
        addCreatureReady(player1, new TymnaTheWeaver());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        markCombatDamage(attacker, player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void triggerStillResolvesAfterTymnaLeavesBattlefield() {
        Permanent tymna = addCreatureReady(player1, new TymnaTheWeaver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        markCombatDamage(attacker, player2);
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(tymna);
        gd.playerGraveyards.get(player1.getId()).add(tymna.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void triggersAgainInAnotherPostcombatMainWithoutNewDamage() {
        addCreatureReady(player1, new TymnaTheWeaver());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        markCombatDamage(attacker, player2);
        GrizzlyBears firstCard = new GrizzlyBears();
        GrizzlyBears secondCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    void tymnasOwnCombatDamageGainsLifeAndEnablesDrawing() {
        Permanent tymna = addCreatureReady(player1, new TymnaTheWeaver());
        tymna.setAttacking(true);
        tymna.setAttackTarget(player2.getId());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private void markCombatDamage(Permanent source, Player damagedPlayer) {
        Set<UUID> damagedPlayers = ConcurrentHashMap.newKeySet();
        damagedPlayers.add(damagedPlayer.getId());
        gd.combatDamageToPlayersThisTurn.put(source.getId(), damagedPlayers);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
