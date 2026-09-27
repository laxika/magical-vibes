package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WeatheredSentinels.class)
class WeatheredSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack a player who did not attack its controller last turn")
    void cannotAttackPlayerWhoDidNotAttackLastTurn() {
        addCreatureReady(player1, new WeatheredSentinels());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack a player who attacked its controller last turn")
    void attacksMatchingPlayerAndGetsAttackTriggerBonuses() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), Set.of(player2.getId()));

        declareAttackers(List.of(0));

        assertThat(sentinels.isAttacking()).isTrue();

        resolveAllTriggers();

        assertThat(sentinels.getEffectivePower()).isEqualTo(5);
        assertThat(sentinels.getEffectiveToughness()).isEqualTo(8);
        assertThat(sentinels.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Attack bonuses wear off at end of turn")
    void attackBonusesWearOffAtEndOfTurn() {
        Permanent sentinels = addCreatureReady(player1, new WeatheredSentinels());
        gd.playersWhoAttackedPlayersLastTurn.put(player1.getId(), Set.of(player2.getId()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinels.getEffectivePower()).isEqualTo(2);
        assertThat(sentinels.getEffectiveToughness()).isEqualTo(5);
        assertThat(sentinels.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
