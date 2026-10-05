package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindGrind.class, Forest.class, GrizzlyBears.class, Divination.class})
class MindGrindTest extends BaseCardTest {

    private void castMindGrind(int xValue) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MindGrind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castAndResolveSorcery(player1, 0, xValue);
    }

    @Test
    @DisplayName("Each opponent reveals until X lands are found and mills every revealed card")
    void millsUntilXLands() {
        harness.setLibrary(player2, List.of(
                new Forest(),        // land 1
                new GrizzlyBears(),
                new Divination(),
                new Forest(),        // land 2 -> stop
                new GrizzlyBears()   // stays in library
        ));

        castMindGrind(2);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Forest", "Forest", "Grizzly Bears", "Divination");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A library with fewer than X lands is entirely milled")
    void millsEntireLibraryWhenFewerLands() {
        harness.setLibrary(player2, List.of(
                new Forest(),
                new GrizzlyBears()
        ));

        castMindGrind(4);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The caster's own library is untouched")
    void doesNotMillController() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));

        castMindGrind(1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting("name").containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name").doesNotContain("Forest", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactly("Forest");
    }

    @Test
    @DisplayName("X cannot be zero when casting Mind Grind")
    void rejectsZeroX() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        MindGrind spell = new MindGrind();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("An opponent with an empty library reveals nothing")
    void emptyLibraryIsUnaffected() {
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of());

        castMindGrind(1);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A landless library puts every revealed card into the graveyard")
    void putsEntireLandlessLibraryIntoGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        Divination sorcery = new Divination();
        harness.setLibrary(player2, List.of(creature, sorcery));
        harness.setGraveyard(player2, List.of());

        castMindGrind(1);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(creature, sorcery);
    }
}
