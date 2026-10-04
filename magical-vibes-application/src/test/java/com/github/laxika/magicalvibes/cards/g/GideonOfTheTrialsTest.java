package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.l.LuxiorGiadasGift;
import com.github.laxika.magicalvibes.cards.a.ApproachOfTheSecondSun;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GideonOfTheTrials.class, DuneBeetle.class, Shock.class,
        LuxiorGiadasGift.class, ApproachOfTheSecondSun.class})
class GideonOfTheTrialsTest extends BaseCardTest {

    @Test
    @DisplayName("+1 prevents combat damage from the targeted permanent")
    void plusOnePreventsCombatDamage() {
        harness.setLife(player1, 20);
        addReadyGideon(player1);
        Permanent attacker = addCreatureReady(player2, new DuneBeetle());

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
        Permanent attacker = addCreatureReady(player2, new DuneBeetle());

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
        harness.castAndResolveInstant(player1, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
    }

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
        harness.castAndResolveInstant(player2, 0, player1.getId());

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
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadyGideon(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GideonOfTheTrials());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    @Test
    void preventionRemainsAfterGideonLeavesBattlefield() {
        Permanent gideon = addReadyGideon(player1);
        Permanent attacker = addCreatureReady(player2, new DuneBeetle());
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(gideon);

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
    }

    @Test
    void preventionExpiresWhenAbilityControllersNextTurnBegins() {
        addReadyGideon(player1);
        Permanent attacker = addCreatureReady(player2, new DuneBeetle());
        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.isPreventedFromDealingDamage(attacker.getId())).isTrue();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.isPreventedFromDealingDamage(attacker.getId())).isFalse();
    }

    @Test
    void animatedGideonDealsFourCombatDamageAndRemainsPlaneswalker() {
        Permanent gideon = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();
        gideon.setAttacking(true);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
    }

    @Test
    void animationAndDamagePreventionEndAfterCleanup() {
        Permanent gideon = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.hasKeyword(gd, gideon, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void emblemProtectsAgainstPoisonAndDrawingFromEmptyLibrary() {
        addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        gd.playerPoisonCounters.put(player1.getId(), 10);
        harness.runStateBasedActions();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.setLibrary(player1, List.of());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emblemDoesNotPreventOpponentFromLosing() {
        addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setLife(player2, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @CardUsed({GideonOfTheTrials.class, ApproachOfTheSecondSun.class})
    void emblemPreventsOpponentsExplicitWinEffect() {
        addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gd.recordSpellCast(player2.getId(), new ApproachOfTheSecondSun());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new ApproachOfTheSecondSun(), "{6}{W}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emblemProtectsAgainAfterAnotherGideonEnters() {
        Permanent first = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(first);
        addReadyGideon(player1);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void emblemDoesNotCountOpponentsGideon() {
        Permanent gideon = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(gideon);
        addReadyGideon(player2);

        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed({GideonOfTheTrials.class, LuxiorGiadasGift.class})
    void emblemStopsProtectingWhenLuxiorRemovesPlaneswalkerType() {
        Permanent gideon = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new LuxiorGiadasGift());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 0, null, gideon.getId());
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, gideon)).isFalse();
        harness.setLife(player1, 0);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
