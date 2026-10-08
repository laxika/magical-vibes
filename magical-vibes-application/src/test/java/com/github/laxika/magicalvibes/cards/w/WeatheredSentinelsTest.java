package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WeatheredSentinels.class)
class WeatheredSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack until a player attacks its controller during that player's last turn")
    void defenderStillAppliesWithoutQualifyingAttack() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());

        assertThat(als.canAttack(gd, sentinels, player1.getId())).isFalse();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can attack a player who attacked its controller during that player's last turn")
    void attacksQualifyingPlayerAndGetsAttackTriggerBonus() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);

        declareAttackers(List.of(0));
        assertThat(sentinels.isAttacking()).isTrue();

        resolveAllTriggers();
        assertThat(sentinels.getPowerModifier()).isEqualTo(3);
        assertThat(sentinels.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The attack bonus wears off at end of turn")
    void attackBonusExpiresAtEndOfTurn() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinels.getPowerModifier()).isZero();
        assertThat(sentinels.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An attack in the current turn does not qualify as an attack during the last turn")
    void currentTurnAttackDoesNotGrantPermission() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.recordPlayerAttackAgainstPlayer(player2.getId(), player1.getId());

        assertThat(als.canAttack(gd, sentinels, player1.getId())).isFalse();
        assertThat(als.canAttackDefender(gd, sentinels, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The attack permission does not bypass summoning sickness")
    void qualifyingAttackDoesNotBypassSummoningSickness() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        sentinels.setSummoningSick(true);
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        assertThat(als.canAttack(gd, sentinels, player1.getId())).isFalse();
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the attacking Sentinels gets the bonus, when its trigger resolves")
    void attackBonusIsTriggeredAndAppliesOnlyToItsSource() {
        Permanent attacker = addCreatureReady(player1, new WeatheredSentinels());
        Permanent nonattacker = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(nonattacker.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A declared attack grants permission after the attacking player's turn ends")
    void declaredAttackIsRememberedForTheNextTurn() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        addCreatureReady(player2, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player2.getId(), new HashSet<>(Set.of(player1.getId())));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, sentinels, player1.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, sentinels, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Indestructible granted by the attack trigger expires at end of turn")
    void attackGrantedIndestructibleExpires() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), new HashSet<>(Set.of(player2.getId())));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
