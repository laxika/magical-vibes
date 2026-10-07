package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeTheBait.class, GrizzlyBears.class, ChandraNalaar.class})
class TakeTheBaitTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage, untaps and goads attackers, and creates another combat")
    void preventsDamageUntapsAndGoadsAttackers() {
        Permanent attackerToPlayer = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackerToPlaneswalker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        gd.combatPhasesThisTurn = 1;
        attackerToPlayer.setAttacking(true);
        attackerToPlayer.setAttackTarget(player2.getId());
        attackerToPlayer.tap();
        attackerToPlaneswalker.setAttacking(true);
        attackerToPlaneswalker.setAttackTarget(planeswalker.getId());
        attackerToPlaneswalker.tap();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();

        assertThat(attackerToPlayer.isTapped()).isFalse();
        assertThat(attackerToPlaneswalker.isTapped()).isFalse();
        assertThat(gqs.isGoaded(gd, attackerToPlayer)).isTrue();
        assertThat(gqs.isGoaded(gd, attackerToPlaneswalker)).isTrue();

        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Can only be cast during an opponent's combat")
    void cannotBeCastDuringYourOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromHand(player1, new TakeTheBait(), "{2}{R}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"BEGINNING_OF_COMBAT", "DECLARE_ATTACKERS",
            "DECLARE_BLOCKERS", "COMBAT_DAMAGE", "END_OF_COMBAT"})
    void cannotBeCastDuringYourOwnCombat(TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new TakeTheBait(), "{2}{R}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"PRECOMBAT_MAIN", "POSTCOMBAT_MAIN", "END_STEP"})
    void cannotBeCastOutsideOpponentsCombat(TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"BEGINNING_OF_COMBAT", "END_OF_COMBAT"})
    void addsCombatEvenWithoutAttackers(TurnStep step) {
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();

        if (step == TurnStep.BEGINNING_OF_COMBAT) {
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        }
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    void goadsUntappedAttackersButDoesNotAffectNonattackersOrLaterAttackers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent idleCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        idleCreature.tap();
        defendingCreature.tap();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(gqs.isGoaded(gd, attacker)).isTrue();
        assertThat(idleCreature.isTapped()).isTrue();
        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(gqs.isGoaded(gd, idleCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, defendingCreature)).isFalse();

        idleCreature.setAttacking(true);
        idleCreature.setAttackTarget(player2.getId());
        assertThat(gqs.isGoaded(gd, idleCreature)).isFalse();
    }

    @Test
    void preventionAlsoAppliesInAdditionalCombatAndGoadExpiresOnCastersNextTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        gd.combatPhasesThisTurn = 1;
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        attacker.tap();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.DECLARE_ATTACKERS);

        assertThat(gqs.isGoaded(gd, attacker)).isTrue();
        declareAttackers(player1, List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gqs.isGoaded(gd, attacker)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isGoaded(gd, attacker)).isFalse();
    }

    @Test
    void doesNotPreventCombatDamageToCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        gd.combatPhasesThisTurn = 1;
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
}
