package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PteronGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Pteron Ghost gives target artifact a regeneration shield")
    void sacrificesAndRegeneratesTargetArtifact() {
        harness.addToBattlefield(player1, new PteronGhost());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, artifact.getId());
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Pteron Ghost");

        harness.passBothPriorities();

        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pteron Ghost cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new PteronGhost());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
