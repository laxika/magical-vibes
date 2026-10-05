package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreSentry.class, GoblinArsonist.class})
class OgreSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Ogre Sentry cannot attack because it has defender")
    void cannotAttackBecauseItHasDefender() {
        addCreatureReady(player1, new OgreSentry());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Defender does not prevent Ogre Sentry from blocking")
    void canBlockWithDefender() {
        addCreatureReady(player1, new GoblinArsonist());
        Permanent sentry = addCreatureReady(player2, new OgreSentry());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(sentry.isBlocking()).isTrue();
        assertThat(sentry.getBlockingTargets()).containsExactly(0);
    }
}
