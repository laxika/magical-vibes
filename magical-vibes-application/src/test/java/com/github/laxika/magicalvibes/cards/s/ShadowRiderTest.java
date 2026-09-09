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

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);
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
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
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

        assertThat(firstBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(5);
        assertThat(secondBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(5);
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

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(6);
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
}
