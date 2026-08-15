package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherTradewindsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one permanent you control and one you don't control to their owners' hands")
    void returnsBothTargetedPermanentsToTheirOwnersHands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(ownPermanentId, opposingPermanentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Requires the first target to be a permanent you control")
    void requiresFirstTargetToBeControlledByCaster() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opposingPermanentId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(opposingPermanentId, ownPermanentId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Requires the second target to be a permanent you don't control")
    void requiresSecondTargetNotToBeControlledByCaster() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new AetherTradewinds()));
        addCastMana();

        UUID firstPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondPermanentId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstPermanentId, secondPermanentId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you don't control");
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
