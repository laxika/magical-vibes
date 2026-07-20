package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ByForceTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 destroys two target artifacts")
    void destroysXArtifacts() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ByForce()));
        harness.addMana(player1, ManaColor.RED, 3); // X=2: {2}{R} = 3

        UUID rodId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");

        harness.castSorcery(player1, 0, 2, List.of(rodId, thopterId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("X=0 destroys nothing")
    void xZeroDoesNothing() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new ByForce()));
        harness.addMana(player1, ManaColor.RED, 1); // X=0: {0}{R} = 1

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Cannot target more artifacts than X")
    void cannotTargetMoreThanX() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new ByForce()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1: {1}{R} = 2

        UUID rodId = harness.getPermanentId(player2, "Rod of Ruin");
        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(rodId, thopterId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ByForce()));
        harness.addMana(player1, ManaColor.RED, 2); // X=1: {1}{R} = 2

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
}
