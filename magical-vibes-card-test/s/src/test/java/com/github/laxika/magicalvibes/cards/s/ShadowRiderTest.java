package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowRider.class, RedwoodTreefolk.class})
class ShadowRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a blocker without flanking -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ShadowRider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker")
    void flankingWeakensEachNonFlankingBlocker() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new RedwoodTreefolk());
        Permanent secondBlocker = addCreatureReady(player2, new RedwoodTreefolk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Flanking's -1/-1 effect wears off at end of turn")
    void flankingPenaltyExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RedwoodTreefolk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(6);
    }

    @Test
    @DisplayName("An unblocked creature with flanking creates no trigger")
    void unblockedCreatesNoTrigger() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Flanking only weakens the non-flanking creature among mixed blockers")
    void flankingOnlyWeakensNonFlankingCreatureAmongMixedBlockers() {
        Permanent attacker = addCreatureReady(player1, new ShadowRider());
        attacker.setAttacking(true);
        Permanent ordinaryBlocker = addCreatureReady(player2, new RedwoodTreefolk());
        Permanent flankingBlocker = addCreatureReady(player2, new ShadowRider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, ordinaryBlocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ordinaryBlocker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, flankingBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, flankingBlocker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not trigger when Shadow Rider blocks")
    void flankingDoesNotTriggerWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new RedwoodTreefolk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ShadowRider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
    }
}
