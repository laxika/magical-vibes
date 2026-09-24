package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpulsiveManeuvers.class, EdgarKingOfFigaro.class, GrizzlyBears.class, HillGiant.class,
        ProdigalPyromancer.class})
class ImpulsiveManeuversTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers once for each creature that attacks, including an opponent's creature")
    void triggersForEachAttacker() {
        addCreatureReady(player1, new ImpulsiveManeuvers());
        Permanent attacker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker2 = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Impulsive Maneuvers"));
        assertThat(gd.stack).extracting(entry -> entry.getTargetId())
                .containsExactlyInAnyOrder(attacker1.getId(), attacker2.getId());
    }

    @Test
    @DisplayName("The coin flip doubles or prevents the attacker's next combat damage")
    void nextCombatDamageIsDoubledOrPrevented() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ImpulsiveManeuvers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        boolean wonFlip = coinFlipWon();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, wonFlip ? 16 : 20);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The combat-only shield does not consume a noncombat damage event")
    void combatOnlyShieldDoesNotAffectNoncombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ImpulsiveManeuvers());
        Permanent attacker = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        boolean wonFlip = coinFlipWon();

        attacker.untap();
        harness.activateAbility(player1, 1, null, victim.getId());
        harness.passBothPriorities();
        assertThat(victim.getMarkedDamage()).isEqualTo(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertLife(player2, wonFlip ? 18 : 20);
    }

    @Test
    @DisplayName("Applies the flip result to all combat damage dealt in one combat damage event")
    void appliesFlipResultToAllCombatDamageInOneEvent() {
        addCreatureReady(player1, new ImpulsiveManeuvers());
        addCreatureReady(player1, new EdgarKingOfFigaro());
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();
        assertThat(gameLogContains("wins the coin flip for Impulsive Maneuvers")).isTrue();

        prepareDeclareBlockers(player1);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blocker1Index = gd.playerBattlefields.get(player2.getId()).indexOf(blocker1);
        int blocker2Index = gd.playerBattlefields.get(player2.getId()).indexOf(blocker2);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blocker1Index, attackerIndex),
                new BlockerAssignment(blocker2Index, attackerIndex)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, attackerIndex, Map.of(
                blocker1.getId(), 2,
                blocker2.getId(), 1));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker1, blocker2);
    }

    private boolean coinFlipWon() {
        assertThat(gameLogContains("coin flip for Impulsive Maneuvers")).isTrue();
        return gameLogContains("wins the coin flip for Impulsive Maneuvers");
    }
}
