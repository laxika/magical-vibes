package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AzureDrake.class, GrizzlyBears.class})
class AzureDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Azure Drake")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new AzureDrake());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying allows Azure Drake to be blocked by another flying creature")
    void flyingCreatureCanBlockAzureDrake() {
        addCreatureReady(player1, new AzureDrake());
        Permanent blocker = addCreatureReady(player2, new AzureDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flying does not prevent Azure Drake from blocking a non-flying creature")
    void flyingDoesNotPreventBlockingNonFlyingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new AzureDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
