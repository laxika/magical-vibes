package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeatheredSentinels.class, GrizzlyBears.class})
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
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        declareAttackers(player2, List.of(0));
        advanceToUpkeep(player1);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).contains(0);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(sentinels.isAttacking()).isTrue();
        assertThat(sentinels.getPowerModifier()).isEqualTo(3);
        assertThat(sentinels.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sentinels, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The attack bonus wears off at end of turn")
    void attackBonusExpiresAtEndOfTurn() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        declareAttackers(player2, List.of(0));
        advanceToUpkeep(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinels.getPowerModifier()).isZero();
        assertThat(sentinels.getToughnessModifier()).isZero();
    }
}
