package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AetherFlash;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrownInShapelessness.class, GrizzlyBears.class, AetherFlash.class})
class DrownInShapelessnessTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castDrownInShapelessness(targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can return a creature you control to its owner's hand")
    void returnsOwnCreatureToOwnersHand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        castDrownInShapelessness(targetId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AetherFlash()).getId();
        harness.setHand(player1, List.of(new DrownInShapelessness()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returns an opponent-owned creature you control to its owner's hand")
    void returnsStolenCreatureToOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        castDrownInShapelessness(target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not resolve when another spell has already returned the target")
    void doesNotResolveAfterTargetLeavesBattlefield() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new DrownInShapelessness(), new DrownInShapelessness()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);

        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof DrownInShapelessness).hasSize(2);
    }

    private void castDrownInShapelessness(UUID targetId) {
        harness.setHand(player1, List.of(new DrownInShapelessness()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
