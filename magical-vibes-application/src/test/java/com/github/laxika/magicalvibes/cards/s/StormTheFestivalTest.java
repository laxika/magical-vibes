package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormTheFestival.class, GrizzlyBears.class, MindStone.class, SerraAngel.class,
        Shock.class, ShivanDragon.class})
class StormTheFestivalTest extends BaseCardTest {

    @Test
    @DisplayName("Puts up to two permanent cards with mana value five or less onto the battlefield")
    void putsUpToTwoEligiblePermanentsOntoBattlefield() {
        Card bears = new GrizzlyBears();
        Card mindStone = new MindStone();
        Card angel = new SerraAngel();
        Card shock = new Shock();
        Card dragon = new ShivanDragon();
        setLibrary(bears, mindStone, angel, shock, dragon);
        harness.setHand(player1, List.of(new StormTheFestival()));
        addMana(3, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getId(), mindStone.getId(), angel.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), mindStone.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(angel, shock, dragon);
    }

    @Test
    @DisplayName("Flashback exiles Storm the Festival after resolving")
    void flashbackExilesAfterResolving() {
        Card bears = new GrizzlyBears();
        setLibrary(bears, new Shock());
        harness.setGraveyard(player1, List.of(new StormTheFestival()));
        addMana(7, 3);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Storm the Festival");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Storm the Festival"));
    }

    private void addMana(int colorless, int green) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.GREEN, green);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
