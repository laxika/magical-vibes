package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaravanVigil.class, Plains.class, Forest.class, Island.class})
class CaravanVigilTest extends BaseCardTest {


    @Test
    @DisplayName("Without morbid, resolving presents basic land search to hand")
    void withoutMorbidPresentsSearchToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.HAND);
    }

    @Test
    @DisplayName("Without morbid, chosen basic land goes to hand")
    void withoutMorbidLandGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("With morbid, the search still offers only basic lands")
    void withMorbidOffersOnlyBasicLands() {
        setupAndCast();
        setupLibrary();
        enableMorbid();

        harness.passBothPriorities();
        answerMorbidChoiceIfPending(true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind())
                .isTrue();
    }

    @Test
    @DisplayName("With morbid, chosen basic land enters the battlefield untapped")
    void withMorbidLandEntersBattlefieldUntapped() {
        setupAndCast();
        setupLibrary();
        enableMorbid();

        harness.passBothPriorities();
        answerMorbidChoiceIfPending(true);

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        answerMorbidChoiceIfPending(true);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && !p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }


    @Test
    @DisplayName("Morbid is checked at resolution time, not cast time")
    void morbidCheckedAtResolution() {
        setupAndCast();
        setupLibrary();

        // No creature has died when casting — enable morbid after casting
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.passBothPriorities();

        answerMorbidChoiceIfPending(true);
        harness.handleCardChosen(player1, 0);
        answerMorbidChoiceIfPending(true);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotInHand(player1, "Plains");
    }


    @Test
    @DisplayName("Player can fail to find")
    void canFailToFind() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Empty library does not prompt for search")
    void emptyLibraryNoPrompt() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("it is empty"));
    }


    @Test
    @DisplayName("With morbid, declining battlefield placement puts the found land into hand")
    void withMorbidCanKeepLandInHand() {
        setupAndCast();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        enableMorbid();

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
            harness.handleCardChosen(player1, 0);
        } else {
            harness.handleCardChosen(player1, 0);
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature death under the caster's control also enables morbid")
    void ownCreatureDeathEnablesMorbid() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.passBothPriorities();
        answerMorbidChoiceIfPending(true);
        harness.handleCardChosen(player1, 0);
        answerMorbidChoiceIfPending(true);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with no basic lands resolves without finding a card")
    void noBasicLandsResolvesWithoutFinding() {
        setupAndCast();
        CaravanVigil remaining = new CaravanVigil();
        harness.setLibrary(player1, List.of(remaining));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Without morbid the selected land is revealed and removed from the library")
    void revealsLandPutIntoHand() {
        setupAndCast();
        Forest forest = new Forest();
        CaravanVigil remaining = new CaravanVigil();
        harness.setLibrary(player1, List.of(forest, remaining));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void answerMorbidChoiceIfPending(boolean accepted) {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, accepted);
        }
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new CaravanVigil()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new CaravanVigil()));
    }

    private void enableMorbid() {
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
    }
}
