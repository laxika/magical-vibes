package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SweetOblivion.class, GrizzlyBears.class})
class SweetOblivionTest extends BaseCardTest {

    @Test
    void millsFourCardsFromTargetPlayer() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setHand(player1, List.of(sweetOblivion));
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        harness.assertInGraveyard(player1, "Sweet Oblivion");
    }

    @Test
    void escapeExilesFourOtherCardsAndExilesSweetOblivionAfterResolution() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        List<Card> otherCards = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, List.of(sweetOblivion, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3)));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(), List.of(), List.of(1, 2, 3, 4), null);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sweetOblivion);
    }

    @Test
    void escapeRequiresFourOtherCardsInTheGraveyard() {
        SweetOblivion sweetOblivion = new SweetOblivion();
        harness.setGraveyard(player1, List.of(sweetOblivion, new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, player2.getId(),
                List.of(), List.of(1, 2, 3), null)).isInstanceOf(IllegalStateException.class);
    }
}
