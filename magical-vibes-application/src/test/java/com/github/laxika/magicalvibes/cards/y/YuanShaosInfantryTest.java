package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YuanShaosInfantry.class, ShuCavalry.class, ShuFootSoldiers.class, RelentlessAssault.class})
class YuanShaosInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone makes Yuan Shao's Infantry unblockable this combat")
    void attacksAloneBecomesUnblockable() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasCantBeBlocked(gd, infantry)).isTrue();
    }

    @Test
    @DisplayName("Attacking alongside another creature leaves Yuan Shao's Infantry blockable")
    void attacksWithOthersStaysBlockable() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());
        addCreatureReady(player1, new ShuCavalry());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasCantBeBlocked(gd, infantry)).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.hasCantBeBlocked(gd, infantry)).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, infantry)).isFalse();
    }

    @Test
    @DisplayName("Attacking alone only makes Yuan Shao's Infantry unblockable for that combat")
    void unblockableExpiresAtEndOfCombat() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());
        addCreatureReady(player1, new ShuCavalry());
        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        harness.passBothPriorities();
        gs.advanceStep(gd);
        gs.advanceStep(gd);

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infantry);
        assertThatCode(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature cannot block the Infantry after its attack-alone trigger resolves")
    void attackingAloneRejectsBlockers() {
        Permanent infantry = addCreatureReady(player1, new YuanShaosInfantry());
        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(infantry);
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
