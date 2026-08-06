package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroundAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Ground Assault deals damage equal to the number of lands you control")
    void dealsDamageEqualToControlledLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GroundAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ground Assault counts all land types, not just basics of one type")
    void survivesWhenNotEnoughLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GroundAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ground Assault counts only your lands, not your opponent's")
    void countsOnlyControllersLands() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GroundAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ground Assault can't target a land")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GroundAssault()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
