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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cybermat.class, Memnite.class, GrizzlyBears.class})
class CybermatTest extends BaseCardTest {

    @Test
    @DisplayName("An unblocked Cybermat gets +X/+0 for attacking artifact creatures")
    void unblockedCybermatGetsArtifactCountBoost() {
        Permanent cybermat = addCreatureReady(player1, new Cybermat());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));
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

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(cybermat.getPowerModifier()).isEqualTo(0);
        assertThat(cybermat.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Blocked artifact attackers count, but nonattacking artifacts do not")
    void countsBlockedArtifactAttackersOnly() {
        Permanent cybermat = addCreatureReady(player1, new Cybermat());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player1, new Memnite());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();

        assertThat(cybermat.getPowerModifier()).isEqualTo(2);
        assertThat(cybermat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The attacking artifact count is evaluated when the ability resolves")
    void countsArtifactsAtResolution() {
        Permanent cybermat = addCreatureReady(player1, new Cybermat());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of());
        assertThat(cybermat.getPowerModifier()).isZero();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(memnite);
        gd.playerGraveyards.get(player1.getId()).add(memnite.getCard());
        harness.passBothPriorities();

        assertThat(cybermat.getPowerModifier()).isEqualTo(1);
        assertThat(cybermat.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Skulk rejects a blocker with greater power before the boost resolves")
    void greaterPowerCreatureCannotBlock() {
        addCreatureReady(player1, new Cybermat());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setPowerModifier(1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }
}
