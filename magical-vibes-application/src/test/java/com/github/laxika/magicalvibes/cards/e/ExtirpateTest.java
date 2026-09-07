package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Extirpate.class, GrizzlyBears.class, Peek.class, Plains.class, Shock.class})
class ExtirpateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all same-name cards from the target card's owner's zones")
    void exilesAllSameNameCardsFromOwnersZones() {
        Card target = new GrizzlyBears();
        Card handCopy = new GrizzlyBears();
        Card libraryCopy = new GrizzlyBears();
        GameData gd = harness.getGameData();

        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setHand(player2, List.of(handCopy, new Peek()));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(libraryCopy);
        gd.playerDecks.get(player2.getId()).add(new Plains());

        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId(), handCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot target a basic land card in a graveyard")
    void cannotTargetBasicLand() {
        Card plains = new Plains();
        harness.setGraveyard(player2, new ArrayList<>(List.of(plains)));
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("basic land");
    }

    @Test
    @DisplayName("Split second prevents a spell response")
    void splitSecondPreventsSpellResponse() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setHand(player1, List.of(new Extirpate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
