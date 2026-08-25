package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castAndResolveEntry(int startingLife) {
        harness.setLife(player1, startingLife);
        harness.setHand(player1, List.of(new SoulgorgerOrgg()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void removeAndResolveLeave() {
        Permanent orgg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SoulgorgerOrgg)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, orgg));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
