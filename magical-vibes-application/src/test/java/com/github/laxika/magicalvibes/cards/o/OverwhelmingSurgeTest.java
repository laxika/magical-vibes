package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingSurge.class, GrizzlyBears.class, Millstone.class, Ornithopter.class})
class OverwhelmingSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 3 damage to target creature")
    void damageModeDealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode destroys target noncreature artifact")
    void destroyModeDestroysNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        cast(new int[]{1}, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Both modes resolve against their targets")
    void bothModesResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Millstone");
    }

    @Test
    @DisplayName("Damage mode cannot target a noncreature artifact")
    void damageModeRejectsNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroy mode cannot target an artifact creature")
    void destroyModeRejectsArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(artifactCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode can target an artifact creature you control")
    void damageModeCanTargetOwnArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Destroy mode cannot target a nonartifact creature")
    void destroyModeRejectsNonartifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact destruction still resolves when the creature target leaves")
    void artifactModeResolvesWhenCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Creature damage still resolves when the artifact target leaves")
    void damageModeResolvesWhenArtifactTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), artifact.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, artifact));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Millstone");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new OverwhelmingSurge()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }
}
