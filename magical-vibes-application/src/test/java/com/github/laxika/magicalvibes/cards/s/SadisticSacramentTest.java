package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SadisticSacramentTest extends BaseCardTest {

    @Test
    void withoutKickerExilesUpToThreeCardsFromTargetLibrary() {
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()
        ));
        harness.setHand(player1, List.of(new SadisticSacrament()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        chooseCards(3);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.exiledCards).allMatch(entry -> entry.faceDown());
    }

    @Test
    void kickedSpellExilesUpToFifteenCardsFromTargetLibrary() {
        harness.setLibrary(player2, IntStream.range(0, 16)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList());
        harness.setHand(player1, List.of(new SadisticSacrament()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, player2.getId(), null);
        harness.passBothPriorities();

        chooseCards(15);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(15);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTargetItsController() {
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new SadisticSacrament()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(libraryCard);
    }

    private void chooseCards(int count) {
        for (int i = 0; i < count; i++) {
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }
    }
}
