package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaithlessLooting.class, GrizzlyBears.class, Island.class})
class FaithlessLootingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting draws two cards then discards two cards")
    void drawsTwoThenDiscardsTwo() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FaithlessLooting(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Hand after casting (spell leaves hand): 2 cards. Draw 2 -> 4 cards.
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // After drawing two, the effect awaits two discard choices.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Net: had 2 (after cast), +2 draw, -2 discard = 2 cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // Two discarded cards plus the resolved Faithless Looting spell itself.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Faithless Looting");
    }

    @Test
    @DisplayName("Cast from hand goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Drew two cards (deck had 2), now discard the two non-Looting cards.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Faithless Looting");
    }

    @Test
    @DisplayName("Flashback casts from graveyard, then the spell is exiled")
    void flashbackCastsThenExiles() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Draw two, then discard two.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Flashback spell is exiled, not returned to graveyard.
        harness.assertNotInGraveyard(player1, "Faithless Looting");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Faithless Looting"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Newly drawn cards can be discarded while keeping the original hand")
    void canDiscardNewlyDrawnCards() {
        GrizzlyBears kept = new GrizzlyBears();
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new FaithlessLooting(), kept));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Faithless Looting");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flashback accepts one red mana and two mana of another color")
    void flashbackAcceptsGenericMana() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Faithless Looting");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Faithless Looting"));
    }

    @Test
    @DisplayName("Flashback still requires sorcery timing")
    void flashbackCannotBeCastDuringUpkeep() {
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Faithless Looting");
        assertThat(gd.stack).isEmpty();
    }
}
