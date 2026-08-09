package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoomingHoverguardTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts target artifact on top of its owner's library")
    void etbPutsTargetArtifactOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new Ornithopter());
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckSizeBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Ornithopter");
        harness.assertOnBattlefield(player1, "Looming Hoverguard");
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LoomingHoverguard()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
