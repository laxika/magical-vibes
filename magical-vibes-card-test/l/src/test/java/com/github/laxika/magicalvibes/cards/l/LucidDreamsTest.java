package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Floodhound;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.v.VerdantCatacombs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LucidDreams.class, Floodhound.class, VerdantCatacombs.class, LensFlare.class, OrnithopterOfParadise.class})
class LucidDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each distinct card type in its controller's graveyard")
    void drawsForEachDistinctCardTypeInOwnGraveyard() {
        Card first = new Floodhound();
        Card second = new VerdantCatacombs();
        Card third = new LensFlare();
        Card fourth = new Floodhound();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setGraveyard(player1, List.of(
                new Floodhound(), new Floodhound(), new VerdantCatacombs(), new LensFlare()));
        harness.setHand(player1, List.of(new LucidDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("Counts only card types in the caster's graveyard")
    void ignoresOpponentGraveyard() {
        Card first = new Floodhound();
        Card second = new VerdantCatacombs();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(new Floodhound()));
        harness.setGraveyard(player2, List.of(new VerdantCatacombs(), new LensFlare()));
        harness.setHand(player1, List.of(new LucidDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("An empty graveyard draws nothing and the resolving spell does not count itself")
    void emptyGraveyardDrawsNothing() {
        Card first = new Floodhound();
        harness.setLibrary(player1, List.of(first));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new LucidDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        harness.assertInGraveyard(player1, "Lucid Dreams");
    }

    @Test
    @DisplayName("An artifact creature contributes both types without counting subtypes")
    void countsAllTypesOnOneCard() {
        Card first = new Floodhound();
        Card second = new LensFlare();
        Card third = new VerdantCatacombs();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(new OrnithopterOfParadise(), new Floodhound()));
        harness.setHand(player1, List.of(new LucidDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Uses the graveyard at resolution and counts a sorcery already there")
    void countsTypesAtResolution() {
        Card first = new Floodhound();
        Card second = new LensFlare();
        Card third = new VerdantCatacombs();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(new Floodhound()));
        harness.setHand(player1, List.of(new LucidDreams()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(new Floodhound(), new LucidDreams()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }
}
