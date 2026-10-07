package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MemoryDrain;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SweetOblivion.class, NyxbornColossus.class, MemoryDrain.class})
class SweetOblivionTest extends BaseCardTest {

    @Test
    void millsFourCardsFromTargetPlayer() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> library = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        harness.setHand(player1, List.of(sweetOblivion));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player1, "Sweet Oblivion");
    }

    @Test
    void escapeExilesFourOtherCardsAndReturnsSweetOblivionToGraveyard() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> otherCards = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        harness.setGraveyard(player1, List.of(sweetOblivion, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3)));
        harness.setLibrary(player2, List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2, 3, 4), null);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sweetOblivion);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);
    }

    @Test
    void escapeRequiresFourOtherCardsInTheGraveyard() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        harness.setGraveyard(player1, List.of(sweetOblivion, new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(),
                List.of(), List.of(1, 2, 3), null)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsOnlyTheTopFourCards() {
        List<Card> library = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus());
        harness.setHand(player1, List.of(new SweetOblivion()));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(4));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library.subList(0, 4));
    }

    @Test
    void canTargetItsControllerWithFewerThanFourCardsRemaining() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> library = List.of(new NyxbornColossus(), new NyxbornColossus());
        harness.setHand(player1, List.of(sweetOblivion));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), sweetOblivion);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void resolvesWhenTargetLibraryIsEmpty() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        harness.setHand(player1, List.of(sweetOblivion));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sweetOblivion);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canEscapeAgainUsingCardsMilledByTheFirstEscape() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> otherCards = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        List<Card> library = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        harness.setGraveyard(player1, List.of(sweetOblivion, otherCards.get(0),
                otherCards.get(1), otherCards.get(2), otherCards.get(3)));
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player1.getId(), List.of(), List.of(1, 2, 3, 4), null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2), library.get(3), sweetOblivion);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        gs.playFlashbackSpell(gd, player1, 4, null, player2.getId(), List.of(), List.of(0, 1, 2, 3), null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sweetOblivion);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(java.util.stream.Stream.concat(
                        otherCards.stream(), library.stream()).toList());
    }

    @Test
    void counteredEscapeReturnsToGraveyardWithoutRefundingExiledCards() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> otherCards = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        List<Card> library = List.of(new NyxbornColossus(), new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus());
        harness.setGraveyard(player1, List.of(sweetOblivion, otherCards.get(0),
                otherCards.get(1), otherCards.get(2), otherCards.get(3)));
        harness.setLibrary(player1, library);
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new MemoryDrain()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        gs.playFlashbackSpell(gd, player1, 0, null, player1.getId(), List.of(), List.of(1, 2, 3, 4), null);
        harness.castInstant(player2, 0, sweetOblivion.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sweetOblivion);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }

    @Test
    void cannotExileItselfToPayEscapeCost() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> graveyard = List.of(sweetOblivion, new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(),
                List.of(), List.of(0, 1, 2, 3), null)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEscapeWithOnlyEnoughManaForNormalCastingCost() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> graveyard = List.of(sweetOblivion, new NyxbornColossus(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(),
                List.of(), List.of(1, 2, 3, 4), null)).isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
