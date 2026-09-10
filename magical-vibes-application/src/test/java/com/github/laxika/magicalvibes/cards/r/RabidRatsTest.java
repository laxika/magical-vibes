package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RabidRats.class, HonorGuard.class, VenerableMonk.class})
class RabidRatsTest extends BaseCardTest {

    private Permanent readyRats() {
        Permanent rats = addCreatureReady(player1, new RabidRats());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return rats;
    }

    private void markAsBlocking(Permanent creature) {
        creature.setBlocking(true);
        creature.addBlockingTarget(0);
    }

    @Test
    @DisplayName("{T}: target blocking creature gets -1/-1 until end of turn")
    void shrinksBlockingCreature() {
        readyRats();
        Permanent blocker = addCreatureReady(player2, new VenerableMonk());
        markAsBlocking(blocker);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking shrink wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        readyRats();
        Permanent blocker = addCreatureReady(player2, new VenerableMonk());
        markAsBlocking(blocker);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-blocking creature is an illegal target")
    void rejectsNonBlockingCreature() {
        readyRats();
        Permanent idle = addCreatureReady(player2, new VenerableMonk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating the ability taps Rabid Rats")
    void activationTapsRats() {
        Permanent rats = readyRats();
        Permanent blocker = addCreatureReady(player2, new VenerableMonk());
        markAsBlocking(blocker);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());

        assertThat(rats.isTapped()).isTrue();

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A creature that stops blocking before resolution is no longer a legal target")
    void targetMustStillBeBlockingOnResolution() {
        readyRats();
        Permanent blocker = addCreatureReady(player2, new VenerableMonk());
        markAsBlocking(blocker);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -1/-1 effect can kill a 1/1 blocking creature")
    void shrinksOneToughnessBlockerToDeath() {
        readyRats();
        Permanent blocker = addCreatureReady(player2, new HonorGuard());
        markAsBlocking(blocker);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Honor Guard");
    }
}
