package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.d.Dismantle;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PteronGhost.class, AetherVial.class, MyrMoonvessel.class, Dismantle.class})
class PteronGhostTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Pteron Ghost gives target artifact a regeneration shield")
    void sacrificesAndRegeneratesTargetArtifact() {
        harness.addToBattlefield(player1, new PteronGhost());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AetherVial());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PteronGhost());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Regeneration shield saves the target artifact from destruction")
    void regenerationShieldSavesTargetArtifact() {
        harness.addToBattlefield(player1, new PteronGhost());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MyrMoonvessel());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Dismantle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertOnBattlefield(player2, "Myr Moonvessel");
        assertThat(artifact.getRegenerationShield()).isZero();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pteron Ghost can regenerate an artifact in response to destruction")
    void regeneratesInResponseToDestruction() {
        harness.addToBattlefield(player1, new PteronGhost());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        harness.setHand(player1, List.of(new Dismantle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, artifact.getId());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.assertInGraveyard(player1, "Pteron Ghost");
        assertThat(gd.stack).hasSize(2);
        assertThat(artifact.getRegenerationShield()).isZero();

        harness.passBothPriorities();
        assertThat(artifact.getRegenerationShield()).isEqualTo(1);
        assertThat(artifact.isTapped()).isFalse();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Aether Vial");
        assertThat(artifact.getRegenerationShield()).isZero();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A regeneration shield protects a noncreature artifact only once")
    void shieldProtectsNoncreatureArtifactOnlyOnce() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new PteronGhost());
        ghost.setTapped(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AetherVial());

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Dismantle(), new Dismantle()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, artifact.getId());
        harness.assertOnBattlefield(player1, "Aether Vial");
        assertThat(artifact.isTapped()).isTrue();

        harness.castAndResolveSorcery(player1, 0, artifact.getId());
        harness.assertNotOnBattlefield(player1, "Aether Vial");
        harness.assertInGraveyard(player1, "Aether Vial");
    }
}
