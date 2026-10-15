package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EscapedNull.class, GlorySeeker.class, EchoCirclet.class})
class EscapedNullTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Escaped Null +5/+0 until end of turn")
    void blockingGivesPlusFivePlusZero() {
        Permanent escapedNull = addReadyNull(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked gives Escaped Null +5/+0 until end of turn")
    void becomingBlockedGivesPlusFivePlusZero() {
        Permanent escapedNull = addReadyNull(player1);
        escapedNull.setAttacking(true);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Becoming blocked triggers only once with multiple blockers")
    void multipleBlockersGiveOnlyOneBoost() {
        Permanent escapedNull = addReadyNull(player1);
        escapedNull.setAttacking(true);
        addReadyAttacker(player2);
        addReadyAttacker(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("The temporary boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent escapedNull = addReadyNull(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(escapedNull.getPowerModifier()).isZero();
        assertThat(escapedNull.getToughnessModifier()).isZero();
        assertThat(escapedNull.getEffectivePower()).isEqualTo(1);
        assertThat(escapedNull.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blocking multiple attackers gives only one boost")
    void blockingMultipleAttackersGivesOnlyOneBoost() {
        Permanent escapedNull = addReadyNull(player2);
        harness.addToBattlefieldAndReturn(player2, new EchoCirclet()).setAttachedTo(escapedNull.getId());
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        ));
        resolveAllTriggers();

        assertThat(escapedNull.getPowerModifier()).isEqualTo(5);
        assertThat(escapedNull.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("Lifelink gains life for all boosted combat damage even when Escaped Null dies")
    void boostedBlockingDamageGainsSixLife() {
        Permanent escapedNull = addReadyNull(player2);
        addReadyAttacker(player1);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(escapedNull);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof EscapedNull);
    }

    @Test
    @DisplayName("An unblocked Escaped Null gets no boost and gains one life")
    void unblockedAttackDoesNotBoost() {
        Permanent escapedNull = addReadyNull(player1);
        escapedNull.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();
        resolveCombat();

        assertThat(escapedNull.getPowerModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private Permanent addReadyNull(Player player) {
        return addCreatureReady(player, new EscapedNull());
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = addCreatureReady(player, new GlorySeeker());
        permanent.setAttacking(true);
    }
}
