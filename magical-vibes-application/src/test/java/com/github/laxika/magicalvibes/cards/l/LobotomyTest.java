package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FaerieConclave;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LobotomyTest extends BaseCardTest {

    @Test
    @DisplayName("Only cards other than basic lands in the revealed hand are choosable")
    void basicLandsAreNotChoosable() {
        castLobotomyAt(new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest(), new FaerieConclave())));

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Grizzly Bears", "Peek", "Faerie Conclave");
    }

    @Test
    @DisplayName("Exiles every copy of the chosen card from the target's hand, graveyard, and library")
    void exilesAllCopiesFromAllZones() {
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        castLobotomyAt(new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek())));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        harness.handleListChoice(player1, "Grizzly Bears");

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears")).count();
        assertThat(exiled).isEqualTo(4);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));

        harness.assertInHand(player2, "Peek");
    }

    @Test
    @DisplayName("A nonbasic land can be chosen and exiled")
    void nonbasicLandCanBeChosen() {
        castLobotomyAt(new ArrayList<>(List.of(new FaerieConclave(), new Peek())));

        harness.handleListChoice(player1, "Faerie Conclave");

        harness.assertNotInHand(player2, "Faerie Conclave");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Faerie Conclave"));
    }

    @Test
    @DisplayName("No choice is made when the revealed hand holds only basic lands")
    void noPromptWhenHandIsAllBasicLands() {
        castLobotomyAt(new ArrayList<>(List.of(new Forest())));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("No choice is made when the target's hand is empty")
    void noPromptWhenHandEmpty() {
        castLobotomyAt(new ArrayList<>());

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster may target themselves")
    void mayTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new Lobotomy(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Grizzly Bears");
    }

    private void castLobotomyAt(List<com.github.laxika.magicalvibes.model.Card> targetHand) {
        harness.setHand(player2, targetHand);
        harness.setHand(player1, new ArrayList<>(List.of(new Lobotomy())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
