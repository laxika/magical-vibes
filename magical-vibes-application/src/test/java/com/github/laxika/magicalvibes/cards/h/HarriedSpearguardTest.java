package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarriedSpearguard.class, GrizzlyBears.class})
class HarriedSpearguardTest extends BaseCardTest {

    @Test
    void createsANonblockingRatWhenItDies() {
        Permanent spearguard = harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, spearguard));

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player1, "Rat");
        assertThat(rats).hasSize(1);
        assertThat(bls.canBlock(gd, rats.getFirst())).isFalse();
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureDies() {
        harness.addToBattlefieldAndReturn(player1, new HarriedSpearguard());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherCreature));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }
}
