package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrownInShapelessness.class, GrizzlyBears.class, AetherFlash.class})
class DrownInShapelessnessTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castDrownInShapelessness(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can return a creature you control to its owner's hand")
    void returnsOwnCreatureToOwnersHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        castDrownInShapelessness(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AetherFlash());
        harness.setHand(player1, List.of(new DrownInShapelessness()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player2, "Aether Flash");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castDrownInShapelessness(UUID targetId) {
        harness.setHand(player1, List.of(new DrownInShapelessness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
