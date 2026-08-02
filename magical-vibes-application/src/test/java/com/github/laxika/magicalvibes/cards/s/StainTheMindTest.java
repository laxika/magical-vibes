package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StainTheMindTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every chosen copy from target player's hand, graveyard and library")
    void exilesChosenCopiesFromAllZones() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        Card peek = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(bears3);

        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count()).isEqualTo(3);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertInHand(player2, "Peek");
        harness.assertInGraveyard(player1, "Stain the Mind");
    }

    @Test
    @DisplayName("Choosing a name with no matches exiles nothing")
    void noMatchesExilesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setGraveyard(player2, List.of());

        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiZoneExileChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Convoke lets tapped creatures pay part of the cost")
    void convokePaysWithTappedCreatures() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        List<Permanent> helpers = List.copyOf(gd.playerBattlefields.get(player1.getId()));

        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                helpers.stream().map(Permanent::getId).toList());

        assertThat(gd.stack).hasSize(1);
        assertThat(helpers).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new StainTheMind()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
