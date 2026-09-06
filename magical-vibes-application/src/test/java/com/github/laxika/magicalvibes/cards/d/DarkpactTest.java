package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Darkpact.class, GrizzlyBears.class, HillGiant.class})
class DarkpactTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges an owned ante card with the top card of the library")
    void exchangesAnteCardWithLibraryTop() {
        Card antedCard = new GrizzlyBears();
        Card libraryTop = new HillGiant();
        harness.setExile(player1, List.of(antedCard));
        gd.markCardAsAnted(antedCard);
        harness.setLibrary(player1, List.of(libraryTop));
        Card spell = new Darkpact();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryTop.getId());
        assertThat(gd.antedCardIds).containsExactly(libraryTop.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Exchanges the ante card into the library when the library is empty")
    void exchangesAnteCardWithEmptyLibrary() {
        Card antedCard = new GrizzlyBears();
        harness.setExile(player1, List.of(antedCard));
        gd.markCardAsAnted(antedCard);
        harness.setLibrary(player1, List.of());
        Card spell = new Darkpact();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, antedCard.getId());

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(antedCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.antedCardIds).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Cannot target a card that is not in the ante")
    void cannotTargetRegularExiledCard() {
        Card exiledCard = new GrizzlyBears();
        harness.setExile(player1, List.of(exiledCard));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ante");
    }

    @Test
    @DisplayName("Cannot target an opponent's ante card")
    void cannotTargetOpponentsAnteCard() {
        Card antedCard = new GrizzlyBears();
        harness.setExile(player2, List.of(antedCard));
        gd.markCardAsAnted(antedCard);
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new Darkpact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, antedCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }
}
