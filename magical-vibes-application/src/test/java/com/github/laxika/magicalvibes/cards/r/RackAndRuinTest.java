package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RackAndRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target artifacts")
    void destroysTwoArtifacts() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID mineId = harness.getPermanentId(player2, "Howling Mine");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castInstant(player1, 0, List.of(mineId, ornithopterId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Howling Mine");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new HowlingMine());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RackAndRuin()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID mineId = harness.getPermanentId(player2, "Howling Mine");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mineId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }
}
