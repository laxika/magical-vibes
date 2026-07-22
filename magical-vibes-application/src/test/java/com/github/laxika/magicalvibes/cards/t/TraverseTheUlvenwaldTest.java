package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TraverseTheUlvenwaldTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, offers only basic lands")
    void withoutDeliriumOffersOnlyBasicLands() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Without delirium, choosing a basic land puts it into hand")
    void withoutDeliriumPutsBasicLandInHand() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("With delirium, offers creatures and lands")
    void withDeliriumOffersCreaturesAndLands() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Ghost Quarter", "Grizzly Bears");
    }

    @Test
    @DisplayName("With delirium, can tutor a creature into hand")
    void withDeliriumCanTutorCreature() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        int creatureIndex = -1;
        for (int i = 0; i < offered.size(); i++) {
            if (offered.get(i).getName().equals("Grizzly Bears")) {
                creatureIndex = i;
                break;
            }
        }
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(creatureIndex));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    private void cast() {
        harness.setHand(player1, List.of(new TraverseTheUlvenwald()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new GhostQuarter(), new GrizzlyBears(), new Shock()));
    }

    private void setupDeliriumGraveyard() {
        // creature + land + instant + artifact = four card types
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(),
                new Forest(),
                new Shock(),
                new Millstone()
        ));
    }
}
