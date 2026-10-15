package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulgorgerOrgg.class})
class SoulgorgerOrggTest extends BaseCardTest {

    @Test
    void losesAllButOneLifeOnEntryAndRegainsTheLossOnLeave() {
        castAndResolveEntry(20);

        harness.assertLife(player1, 1);

        removeAndResolveLeave();

        harness.assertLife(player1, 20);
    }

    @Test
    void leavesTriggerUsesTheAmountLostOnEntry() {
        castAndResolveEntry(20);
        harness.setLife(player1, 6);

        removeAndResolveLeave();

        harness.assertLife(player1, 25);
    }

    @Test
    void noLifeIsGainedIfNoLifeWasLostOnEntry() {
        castAndResolveEntry(1);

        removeAndResolveLeave();

        harness.assertLife(player1, 1);
    }

    @Test
    void leavesTriggerAlsoFiresWhenItReturnsToHand() {
        castAndResolveEntry(12);

        Permanent orgg = findPermanent(player1, "Soulgorger Orgg");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, orgg));

        resolveLeaveTrigger();

        harness.assertLife(player1, 12);
    }

    private void castAndResolveEntry(int startingLife) {
        harness.setLife(player1, startingLife);
        harness.castFromHand(player1, new SoulgorgerOrgg(), "{3}{R}{R}");
        resolveAllTriggers();
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

    @Test
    void entryUsesLifeTotalWhenItsTriggerResolves() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SoulgorgerOrgg(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.setLife(player1, 9);

        harness.passBothPriorities();
        harness.assertLife(player1, 1);

        removeAndResolveLeave();

        harness.assertLife(player1, 9);
    }

    @Test
    void eachOrggRemembersOnlyItsOwnLifeLoss() {
        castAndResolveEntry(20);
        Permanent first = findPermanent(player1, "Soulgorger Orgg");
        castAndResolveEntry(8);
        Permanent second = findPermanents(player1, "Soulgorger Orgg").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        resolveLeaveTrigger();
        harness.assertLife(player1, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second));
        resolveLeaveTrigger();
        harness.assertLife(player1, 27);
    }
}
