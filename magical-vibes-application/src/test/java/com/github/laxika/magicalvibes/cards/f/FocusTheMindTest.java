package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FocusTheMind.class, Fog.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class})
class FocusTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Costs the full amount before another spell is cast")
    void costsFullAmountBeforeAnotherSpell() {
        harness.setHand(player1, List.of(new FocusTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs two less after another spell is cast this turn")
    void costsTwoLessAfterAnotherSpell() {
        harness.setHand(player1, List.of(new Fog(), new FocusTheMind()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Draws three cards, then discards one card")
    void drawsThreeThenDiscardsOne() {
        harness.setHand(player1, List.of(new FocusTheMind(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, findCardIndex("Grizzly Bears"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Focus the Mind");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Another spell need not resolve before the cost is reduced")
    void costsTwoLessWhileAnotherSpellIsOnStack() {
        harness.setHand(player1, List.of(new Fog(), new FocusTheMind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's spell does not reduce the cost")
    void opponentSpellDoesNotReduceCost() {
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new FocusTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casting a creature also reduces the cost")
    void creatureSpellReducesCost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new FocusTheMind()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A newly drawn card can be discarded when the hand was otherwise empty")
    void canDiscardNewlyDrawnCard() {
        harness.setHand(player1, List.of(new FocusTheMind()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, findCardIndex("Island"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Focus the Mind");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    private int findCardIndex(String cardName) {
        return IntStream.range(0, gd.playerHands.get(player1.getId()).size())
                .filter(index -> gd.playerHands.get(player1.getId()).get(index).getName().equals(cardName))
                .findFirst()
                .orElseThrow();
    }
}
