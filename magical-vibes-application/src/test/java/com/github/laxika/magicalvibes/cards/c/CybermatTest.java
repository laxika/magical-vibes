package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cybermat.class, Memnite.class, GrizzlyBears.class})
class CybermatTest extends BaseCardTest {

    @Test
    @DisplayName("An unblocked Cybermat gets +X/+0 for attacking artifact creatures")
    void unblockedCybermatGetsArtifactCountBoost() {
        Permanent cybermat = addCreatureReady(player1, new Cybermat());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        Permanent nonArtifact = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        cybermat.setAttacking(true);
        memnite.setAttacking(true);
        nonArtifact.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(cybermat.getPowerModifier()).isEqualTo(2);
        assertThat(cybermat.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A blocked Cybermat does not get the boost")
    void blockedCybermatDoesNotGetBoost() {
        Permanent cybermat = addCreatureReady(player1, new Cybermat());
        addCreatureReady(player2, new GrizzlyBears());

        cybermat.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(cybermat.getPowerModifier()).isEqualTo(0);
        assertThat(cybermat.getToughnessModifier()).isEqualTo(0);
    }
}
