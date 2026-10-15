package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighbornGhoul.class, YoungWolf.class, OneEyedScarecrow.class})
class HighbornGhoulTest extends BaseCardTest {

    @Test
    void greenNonartifactCreatureCannotBlock() {
        addCreatureReady(player1, new HighbornGhoul());
        addCreatureReady(player2, new YoungWolf());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void creatureSharingBlackCanBlock() {
        addCreatureReady(player1, new HighbornGhoul());
        Permanent blocker = addCreatureReady(player2, new HighbornGhoul());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void artifactCreatureWithoutSharedColorCanBlock() {
        addCreatureReady(player1, new HighbornGhoul());
        Permanent blocker = addCreatureReady(player2, new OneEyedScarecrow());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateDoesNotRestrictGhoulsOwnBlocking() {
        addCreatureReady(player1, new YoungWolf());
        Permanent blocker = addCreatureReady(player2, new HighbornGhoul());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
