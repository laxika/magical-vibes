package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoldTheLine.class, MossKami.class})
class HoldTheLineTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking creatures get +7/+7, but other creatures do not")
    void boostsBlockingCreaturesOnly() {
        Permanent attacker = addCreatureReady(player1, new MossKami());

        Permanent blocker = addCreatureReady(player2, new MossKami());
        Permanent bystander = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castHoldTheLine();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bystander)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bystander)).isEqualTo(5);
    }

    @Test
    @DisplayName("The blocking-creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blocker = addCreatureReady(player2, new MossKami());
        addCreatureReady(player1, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castHoldTheLine();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(12);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Every blocking creature is boosted when the defending player casts the spell")
    void boostsAllBlockersForDefendingCaster() {
        addCreatureReady(player1, new MossKami());
        addCreatureReady(player1, new MossKami());
        Permanent firstBlocker = addCreatureReady(player2, new MossKami());
        Permanent secondBlocker = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

        harness.castFromHand(player2, new HoldTheLine(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(12);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(12);
    }

    @Test
    @DisplayName("Resolving before combat does not boost creatures that block later")
    void doesNotBoostLaterBlockers() {
        addCreatureReady(player1, new MossKami());
        Permanent blocker = addCreatureReady(player2, new MossKami());

        castHoldTheLine();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof HoldTheLine);
    }

    @Test
    @DisplayName("The boost remains after combat ends")
    void boostRemainsAfterCombat() {
        addCreatureReady(player1, new MossKami());
        Permanent blocker = addCreatureReady(player2, new MossKami());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        castHoldTheLine();

        harness.passUntil(TurnStep.COMBAT_DAMAGE);
        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), gqs.getEffectivePower(gd, attacker)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(12);
    }

    private void castHoldTheLine() {
        harness.castFromHand(player1, new HoldTheLine(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
