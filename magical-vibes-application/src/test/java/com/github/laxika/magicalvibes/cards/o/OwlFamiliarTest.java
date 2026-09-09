package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwlFamiliar.class, Forest.class})
class OwlFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then discards a card (net hand size unchanged)")
    void etbDrawThenDiscard() {
        harness.setLibrary(player1, List.of(new Forest()));
        castOwlFamiliar();

        harness.passBothPriorities(); // resolve creature spell

        // ETB trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve ETB trigger — draws, then prompts discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        // Drew 1 (Forest), discarded 1 → hand size unchanged
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB lets the controller discard a pre-existing card after drawing")
    void etbDrawsBeforeChoosingDiscard() {
        harness.setHand(player1, List.of(new OwlFamiliar(), new Forest()));
        harness.setLibrary(player1, List.of(new OwlFamiliar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Owl Familiar");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Owl Familiar");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature enters the battlefield")
    void creatureEntersBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        castOwlFamiliar();
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Owl Familiar");
    }

    private void castOwlFamiliar() {
        harness.castFromHand(player1, new OwlFamiliar(), "{1}{U}");
    }
}
