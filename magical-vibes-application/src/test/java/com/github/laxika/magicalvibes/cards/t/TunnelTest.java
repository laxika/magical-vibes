package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Wall")
    void destroysWall() {
        harness.addToBattlefield(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Tunnel()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Wall");
        harness.assertInGraveyard(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("Destroyed Wall cannot be regenerated")
    void wallCannotBeRegenerated() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        wall.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Tunnel()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Wall");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Wall");
        harness.assertInGraveyard(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("Cannot target a non-Wall creature")
    void cannotTargetNonWall() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Tunnel()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
