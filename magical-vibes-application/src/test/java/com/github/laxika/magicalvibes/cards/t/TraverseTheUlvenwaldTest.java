package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TraverseTheUlvenwald.class, Forest.class, EvolvingWilds.class, YoungWolf.class, TragicSlip.class, TravelersAmulet.class})
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

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Forest");
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
                .containsExactlyInAnyOrder("Forest", "Evolving Wilds", "Young Wolf");
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
            if (offered.get(i).getName().equals("Young Wolf")) {
                creatureIndex = i;
                break;
            }
        }
        assertThat(creatureIndex).isGreaterThanOrEqualTo(0);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, creatureIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Young Wolf");
    }

    @Test
    void withDeliriumCanTutorNonbasicLand() {
        setupDeliriumGraveyard();
        EvolvingWilds land = new EvolvingWilds();
        harness.setLibrary(player1, List.of(land, new TragicSlip()));
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canFailToFindEvenWithMatchingBasicLand() {
        setupLibrary();
        cast();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void threeTypesDoNotCountResolvingSpellOrDuplicateCards() {
        harness.setGraveyard(player1, List.of(new Forest(), new TragicSlip(), new YoungWolf(), new YoungWolf()));
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void deliriumGainedAfterCastingUpgradesSearch() {
        setupLibrary();
        cast();
        setupDeliriumGraveyard();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactlyInAnyOrder("Forest", "Evolving Wilds", "Young Wolf");
    }

    @Test
    void deliriumLostAfterCastingUsesBasicLandSearch() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();
        harness.setGraveyard(player1, List.of(new Forest(), new TragicSlip(), new YoungWolf()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player2, List.of(new Forest(), new TragicSlip(), new YoungWolf(), new TravelersAmulet()));
        setupLibrary();
        cast();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void noMatchingCardsCompletesWithoutAddingToHand() {
        harness.setLibrary(player1, List.of(new EvolvingWilds(), new YoungWolf(), new TragicSlip()));
        cast();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new TraverseTheUlvenwald()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new EvolvingWilds(), new YoungWolf(), new TragicSlip()));
    }

    private void setupDeliriumGraveyard() {
        // creature + land + instant + artifact = four card types
        harness.setGraveyard(player1, List.of(
                new YoungWolf(),
                new Forest(),
                new TragicSlip(),
                new TravelersAmulet()
        ));
    }
}
