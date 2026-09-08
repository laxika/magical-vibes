package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RainOfRustTest extends BaseCardTest {

    @Test
    @DisplayName("The artifact mode destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(new int[]{0}, List.of(artifact.getId()), false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact).contains(land);
    }

    @Test
    @DisplayName("The land mode destroys a target land")
    void destroysTargetLand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(new int[]{1}, List.of(land.getId()), false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact).doesNotContain(land);
    }

    @Test
    @DisplayName("Entwine pays the additional mana and resolves both modes")
    void entwineDestroysArtifactAndLand() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        cast(new int[]{0, 1}, List.of(artifact.getId(), land.getId()), true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact, land);
    }

    @Test
    @DisplayName("The two modes may target the same artifact land")
    void entwineMayTargetTheSameArtifactLand() {
        Permanent artifactLand = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());

        cast(new int[]{0, 1}, List.of(artifactLand.getId(), artifactLand.getId()), true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifactLand);
    }

    @Test
    @DisplayName("The artifact mode rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RainOfRust()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, boolean entwined) {
        harness.setHand(player1, List.of(new RainOfRust()));
        if (entwined) {
            harness.addMana(player1, ManaColor.RED, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 6);
        } else {
            addBaseMana();
        }
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
