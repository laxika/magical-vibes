package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ContestedGameBall.class, PanickedAltisaur.class, Abrade.class, CaptainsManeuver.class})
class ContestedGameBallTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage transfers control and untaps the ball")
    void combatDamageTransfersControlAndUntaps() {
        Permanent ball = addBall(player2);
        ball.tap();
        Permanent attacker = addCreatureReady(player1, new PanickedAltisaur());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ball);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(ball);
        assertThat(ball.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability draws and adds a point counter")
    void activatedAbilityDrawsAndAddsPointCounter() {
        Permanent ball = addBall(player1);
        harness.setLibrary(player1, List.of(new PanickedAltisaur()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(ball.getCounterCount(CounterType.POINT)).isEqualTo(1);
        assertThat(ball.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The fifth point counter sacrifices the ball and creates a Treasure")
    void fifthPointCounterSacrificesAndCreatesTreasure() {
        Permanent ball = addBall(player1);
        ball.setCounterCount(CounterType.POINT, 4);
        harness.setLibrary(player1, List.of(new PanickedAltisaur()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ball);
        harness.assertInGraveyard(player1, "Contested Game Ball");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    @Test
    @DisplayName("Combat damage leaves the ball with its controller until the trigger resolves")
    void controlChangesOnlyWhenTriggerResolves() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        Permanent ball = addBall(player2);
        ball.tap();
        Permanent attacker = addCreatureReady(player1, new PanickedAltisaur());
        attacker.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player1));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ball);
        assertThat(ball.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ball);
        assertThat(ball.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Simultaneous combat damage from two creatures triggers only once")
    void simultaneousCombatDamageTriggersOnce() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        Permanent ball = addBall(player2);
        addCreatureReady(player1, new PanickedAltisaur()).setAttacking(true);
        addCreatureReady(player1, new PanickedAltisaur()).setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player1));

        harness.assertLife(player2, 12);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ball);

        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ball);
    }

    @Test
    @DisplayName("Noncombat damage does not transfer control or untap the ball")
    void noncombatDamageDoesNotTransferControl() {
        Permanent ball = addBall(player2);
        ball.tap();
        addCreatureReady(player1, new PanickedAltisaur());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ball);
        assertThat(ball.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A destroyed ball with five point counters still creates Treasure on resolution")
    void destroyedBallWithFiveCountersStillCreatesTreasure() {
        Permanent ball = addBall(player1);
        ball.setCounterCount(CounterType.POINT, 5);
        harness.setLibrary(player1, List.of(new PanickedAltisaur()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castModalInstant(player2, 0, 1, List.of(ball.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Contested Game Ball");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Panicked Altisaur");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @DisplayName("A destroyed ball with four point counters draws without creating Treasure")
    void destroyedBallBelowThresholdStillDraws() {
        Permanent ball = addBall(player1);
        ball.setCounterCount(CounterType.POINT, 4);
        harness.setLibrary(player1, List.of(new PanickedAltisaur()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castModalInstant(player2, 0, 1, List.of(ball.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Contested Game Ball");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Panicked Altisaur");
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Combat damage redirected to the attacking player untaps their ball on resolution")
    void redirectedCombatDamageUntapsAttackingPlayersBall() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN,
                TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.PRECOMBAT_MAIN,
                TurnStep.POSTCOMBAT_MAIN, TurnStep.DECLARE_BLOCKERS, TurnStep.COMBAT_DAMAGE));
        Permanent ball = addBall(player1);
        ball.tap();
        Permanent attacker = addCreatureReady(player1, new PanickedAltisaur());
        harness.setHand(player1, List.of(new CaptainsManeuver()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstantForX(player1, 0, 4, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();
        attacker.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> resolveCombat(player1));

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        assertThat(ball.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ball);
        assertThat(ball.isTapped()).isFalse();
    }

    private Permanent addBall(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ContestedGameBall());
    }
}
