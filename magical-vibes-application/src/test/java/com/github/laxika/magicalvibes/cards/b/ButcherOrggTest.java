package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ButcherOrgg.class, GrizzlyBears.class})
class ButcherOrggTest extends BaseCardTest {

    @Test
    @DisplayName("Butcher Orgg can divide its combat damage among defending creatures and player")
    void dividesBlockedCombatDamageAmongDefendingCreaturesAndPlayer() {
        harness.setLife(player2, 20);
        Permanent orgg = addCreatureReady(player1, new ButcherOrgg());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        orgg.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        advanceToCombatDamageAssignment();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.validTargets()).extracting(target -> target.id())
                .contains(blocker.getId(), otherCreature.getId(), player2.getId());

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                otherCreature.getId(), 2,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId())
                        || permanent.getId().equals(otherCreature.getId()));
    }

    @Test
    @DisplayName("Butcher Orgg can divide unblocked combat damage among defending creatures and player")
    void dividesUnblockedCombatDamageAmongDefendingCreatureAndPlayer() {
        harness.setLife(player2, 20);
        Permanent orgg = addCreatureReady(player1, new ButcherOrgg());
        Permanent defendingCreature = addCreatureReady(player2, new GrizzlyBears());
        orgg.setAttacking(true);

        advanceToCombatDamageAssignment();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(
                player1, 0, Map.of(orgg.getId(), 6)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                defendingCreature.getId(), 1,
                player2.getId(), 5));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(defendingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Butcher Orgg does not prompt when the defending player controls no creatures")
    void doesNotPromptWithoutDefendingCreatures() {
        harness.setLife(player2, 20);
        Permanent orgg = addCreatureReady(player1, new ButcherOrgg());
        orgg.setAttacking(true);

        advanceToCombatDamageAssignment();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    private void advanceToCombatDamageAssignment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
