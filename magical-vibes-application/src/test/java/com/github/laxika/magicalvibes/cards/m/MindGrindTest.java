package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindGrindTest extends BaseCardTest {

    private void castMindGrind(int xValue) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new MindGrind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.castSorcery(player1, 0, xValue);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each opponent reveals until X lands are found and mills every revealed card")
    void millsUntilXLands() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
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
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(
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
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new GrizzlyBears()));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Forest()));

        castMindGrind(1);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting("name").containsExactly("Forest", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting("name").doesNotContain("Forest", "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name").containsExactly("Forest");
    }
}
