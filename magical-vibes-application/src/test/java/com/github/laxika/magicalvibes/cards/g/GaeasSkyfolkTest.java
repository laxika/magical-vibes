package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaeasSkyfolk.class, MournfulZombie.class})
class GaeasSkyfolkTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Gaea's Skyfolk")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new GaeasSkyfolk());
        addCreatureReady(player2, new MournfulZombie());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying allows Gaea's Skyfolk to be blocked by another flying creature")
    void flyingAllowsAnotherFlyingCreatureToBlock() {
        addCreatureReady(player1, new GaeasSkyfolk());
        Permanent blocker = addCreatureReady(player2, new GaeasSkyfolk());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
