package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineSacrament.class, Cancel.class})
class SanguineSacramentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving gains twice X life (X=3 gains 6)")
    void gainsTwiceXLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("X=0 gains no life")
    void xZeroGainsNoLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isInstanceOf(SanguineSacrament.class);
        harness.assertNotInGraveyard(player1, "Sanguine Sacrament");
    }

    @Test
    @DisplayName("X=5 gains 10 life")
    void xFiveGainsTenLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castInstant(player1, 0, 5, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to bottom of owner's library after resolving, not graveyard")
    void goesToBottomOfLibraryAfterResolving() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Not in graveyard
        harness.assertNotInGraveyard(player1, "Sanguine Sacrament");
        // On the bottom of the library (last element in the deck list)
        List<?> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(((com.github.laxika.magicalvibes.model.Card) deck.getLast()).getName())
                .isEqualTo("Sanguine Sacrament");
    }

    @Test
    @DisplayName("Not in exile after resolving")
    void notInExileAfterResolving() {
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.exiledCards)
                .noneMatch(e -> e.card().getName().equals("Sanguine Sacrament"));
    }

    @Test
    @DisplayName("Can pay X with any color mana beyond the WW base cost")
    void canPayXWithAnyColor() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new SanguineSacrament()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Resolving into an empty library returns the spell as its only card")
    void resolvesIntoEmptyLibrary() {
        SanguineSacrament sacrament = new SanguineSacrament();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(sacrament));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sacrament);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Sanguine Sacrament");
    }

    @Test
    @DisplayName("A countered spell gains no life and goes to the graveyard")
    void counteredSpellDoesNotReturnToLibrary() {
        SanguineSacrament sacrament = new SanguineSacrament();
        harness.setHand(player1, List.of(sacrament));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sacrament.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Sanguine Sacrament");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sacrament);
        assertThat(gd.stack).isEmpty();
    }
}
