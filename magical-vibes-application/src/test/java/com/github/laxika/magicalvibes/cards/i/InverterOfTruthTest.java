package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InverterOfTruth.class, Forest.class, GrizzlyBears.class})
class InverterOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the library face down and shuffles the graveyard into it")
    void exilesLibraryFaceDownAndShufflesGraveyardIntoLibrary() {
        Card libraryCard = new Forest();
        Card secondLibraryCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard, secondLibraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new InverterOfTruth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactly(libraryCard, secondLibraryCard);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
    }
}
