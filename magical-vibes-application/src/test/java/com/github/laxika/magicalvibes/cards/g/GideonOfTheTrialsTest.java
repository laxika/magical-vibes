package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GideonOfTheTrialsTest extends BaseCardTest {

    // ===== +1: Until your next turn, prevent all damage target permanent would deal =====

    @Test
    @DisplayName("+1 prevents combat damage from the targeted permanent")
    void plusOnePreventsCombatDamage() {
        harness.setLife(player1, 20);
        addReadyGideon(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        // +1 targeting the opponent's attacker
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        // On the opponent's turn the attacker swings — its damage is prevented
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("+1 prevention lasts until your next turn (survives the cleanup step)")
    void plusOnePreventionSurvivesEndOfTurn() {
        addReadyGideon(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.isPreventedFromDealingDamage(attacker.getId())).isTrue();

        // Advance through this turn's cleanup — a "this turn" prevention would clear here.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.isPreventedFromDealingDamage(attacker.getId())).isTrue();
    }

    // ===== 0: becomes a 4/4 Human Soldier with indestructible; prevent all damage to him =====

    @Test
    @DisplayName("0 animates Gideon into an indestructible creature")
    void zeroAnimatesIntoIndestructibleCreature() {
        Permanent gideon = addReadyGideon(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("0 prevents all damage dealt to Gideon this turn (no loyalty loss)")
    void zeroPreventsDamageToGideon() {
        Permanent gideon = addReadyGideon(player1);
        int loyaltyBefore = gideon.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Damage aimed at Gideon this turn is prevented, so no loyalty is lost.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
    }

    // ===== 0: emblem — you can't lose the game while you control a Gideon planeswalker =====

    @Test
    @DisplayName("Emblem stops the controller losing at 0 life while a Gideon is in play")
    void emblemPreventsLossWhileControllingGideon() {
        addReadyGideon(player1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player1, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Emblem no longer protects once the controller has no Gideon planeswalker")
    void emblemStopsProtectingWhenGideonLeaves() {
        addReadyGideon(player1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Gideon leaves the battlefield — the emblem's condition is no longer met.
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.setLife(player1, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    // ===== Helpers =====

    private Permanent addReadyGideon(Player player) {
        Permanent perm = new Permanent(new GideonOfTheTrials());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
