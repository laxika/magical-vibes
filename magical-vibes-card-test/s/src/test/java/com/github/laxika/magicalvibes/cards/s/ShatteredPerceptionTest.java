package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.cards.t.ThoughtScour;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteredPerception.class, YoungWolf.class, FaithlessLooting.class, ThoughtScour.class, AlmsCollector.class})
class ShatteredPerceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting discards remaining hand then draws that many cards")
    void discardsHandThenDrawsThatMany() {
        harness.setLibrary(player1, List.of(new FaithlessLooting(), new FaithlessLooting(), new FaithlessLooting()));
        harness.setHand(player1, List.of(
                new ShatteredPerception(),
                new YoungWolf(),
                new YoungWolf(),
                new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // After casting, hand had 3 cards (spell left hand). Discard 3, draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Faithless Looting"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Shattered Perception");
        harness.assertInGraveyard(player1, "Young Wolf");
    }

    @Test
    @DisplayName("With empty hand after casting, discards nothing and draws nothing")
    void emptyHandDoesNothing() {
        harness.setLibrary(player1, List.of(new FaithlessLooting()));
        harness.setHand(player1, List.of(new ShatteredPerception()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Shattered Perception");
    }

    @Test
    @DisplayName("Cast from hand goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setLibrary(player1, List.of(new FaithlessLooting(), new FaithlessLooting()));
        harness.setHand(player1, List.of(new ShatteredPerception(), new YoungWolf()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shattered Perception");
    }

    @Test
    @DisplayName("Flashback casts from graveyard, then the spell is exiled")
    void flashbackCastsThenExiles() {
        harness.setLibrary(player1, List.of(new FaithlessLooting(), new FaithlessLooting(), new FaithlessLooting()));
        harness.setGraveyard(player1, List.of(new ShatteredPerception()));
        harness.setHand(player1, List.of(new YoungWolf(), new YoungWolf(), new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Faithless Looting"));
        harness.assertNotInGraveyard(player1, "Shattered Perception");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shattered Perception"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setLibrary(player1, List.of(new FaithlessLooting(), new FaithlessLooting(), new FaithlessLooting()));
        harness.setGraveyard(player1, List.of(new ShatteredPerception()));
        harness.setHand(player1, List.of(new YoungWolf()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal casting accepts exactly two generic mana and one red mana")
    void normalCastUsesPrintedManaCost() {
        harness.setHand(player1, List.of(new ShatteredPerception()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shattered Perception");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback with an empty hand draws nothing and still exiles the spell")
    void emptyHandFlashbackStillExiles() {
        ShatteredPerception spell = new ShatteredPerception();
        FaithlessLooting undrawn = new FaithlessLooting();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Flashback requires red mana even when six generic mana are available")
    void flashbackRequiresRedMana() {
        ShatteredPerception spell = new ShatteredPerception();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discards the hand present at resolution and leaves the opponent's hand alone")
    void countsHandAtResolution() {
        ShatteredPerception spell = new ShatteredPerception();
        YoungWolf original = new YoungWolf();
        YoungWolf drawnInResponse = new YoungWolf();
        FaithlessLooting first = new FaithlessLooting();
        FaithlessLooting second = new FaithlessLooting();
        FaithlessLooting opponentCard = new FaithlessLooting();
        harness.setHand(player1, List.of(spell, new ThoughtScour(), original));
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(drawnInResponse, first, second));
        harness.setLibrary(player2, List.of(new YoungWolf(), new YoungWolf(), new YoungWolf()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original, drawnInResponse, spell);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot be cast while another spell is on the stack")
    void flashbackRequiresEmptyStack() {
        ShatteredPerception flashbackSpell = new ShatteredPerception();
        harness.setGraveyard(player1, List.of(flashbackSpell));
        harness.setHand(player1, List.of(new ShatteredPerception()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(flashbackSpell);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Alms Collector replaces the entire multi-card draw with one card for each player")
    void almsCollectorReplacesMultiCardDraw() {
        FaithlessLooting controllerDraw = new FaithlessLooting();
        FaithlessLooting remaining = new FaithlessLooting();
        YoungWolf opponentDraw = new YoungWolf();
        harness.addToBattlefield(player2, new AlmsCollector());
        harness.setHand(player1, List.of(new ShatteredPerception(), new YoungWolf(), new YoungWolf()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(controllerDraw, remaining));
        harness.setLibrary(player2, List.of(opponentDraw));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

}
