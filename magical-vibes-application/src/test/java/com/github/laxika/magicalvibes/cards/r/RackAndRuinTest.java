package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GrimMonolith;
import com.github.laxika.magicalvibes.cards.j.JhoirasToolbox;
import com.github.laxika.magicalvibes.cards.t.ThranLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RackAndRuin.class, GrimMonolith.class, JhoirasToolbox.class, ThranLens.class,
        GiantCockroach.class})
class RackAndRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target artifacts")
    void destroysTwoArtifacts() {
        Permanent toolbox = harness.addToBattlefieldAndReturn(player2, new JhoirasToolbox());
        Permanent lens = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(toolbox.getId(), lens.getId()));

        harness.assertNotOnBattlefield(player2, "Jhoira's Toolbox");
        harness.assertNotOnBattlefield(player2, "Thran Lens");
        harness.assertInGraveyard(player2, "Jhoira's Toolbox");
        harness.assertInGraveyard(player2, "Thran Lens");
    }

    @Test
    @DisplayName("Destroys the remaining legal target if the other target leaves")
    void destroysRemainingTargetWhenOtherLeaves() {
        Permanent leavingArtifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        Permanent remainingArtifact = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(leavingArtifact.getId(), remainingArtifact.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(leavingArtifact);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Thran Lens");
        harness.assertInGraveyard(player2, "Thran Lens");
    }

    @Test
    @DisplayName("Can target artifacts controlled by either player")
    void canTargetArtifactsControlledByEitherPlayer() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownArtifact.getId(), opposingArtifact.getId()));

        harness.assertInGraveyard(player1, "Grim Monolith");
        harness.assertInGraveyard(player2, "Thran Lens");
    }

    @Test
    @DisplayName("Requires exactly two artifact targets")
    void requiresExactlyTwoTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two different artifact targets")
    void requiresTwoDifferentTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ThranLens());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
}
