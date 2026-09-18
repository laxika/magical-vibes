package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SoulgorgerOrgg.class)
class SoulgorgerOrggTest extends BaseCardTest {

    @Test
    void losesAllButOneLifeOnEntryAndRegainsTheLossOnLeave() {
        castAndResolveEntry(20);

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);

        removeAndResolveLeave();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void leavesTriggerUsesTheAmountLostOnEntry() {
        castAndResolveEntry(20);
        harness.setLife(player1, 6);

        removeAndResolveLeave();

        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    void noLifeIsGainedIfNoLifeWasLostOnEntry() {
        castAndResolveEntry(1);

        removeAndResolveLeave();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    @Test
    void leavesTriggerAlsoFiresWhenReturnedToHand() {
        castAndResolveEntry(10);

        Permanent orgg = findPermanent(player1, "Soulgorger Orgg");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, orgg));
        resolveLeaveTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    void leavingBeforeEntryTriggerResolvesDoesNotRestoreLife() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SoulgorgerOrgg(), "{3}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        removeAndResolveLeave();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    private void castAndResolveEntry(int startingLife) {
        harness.setLife(player1, startingLife);
        harness.castFromHand(player1, new SoulgorgerOrgg(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void removeAndResolveLeave() {
        Permanent orgg = findPermanent(player1, "Soulgorger Orgg");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, orgg));

        resolveLeaveTrigger();
    }

    private void resolveLeaveTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
