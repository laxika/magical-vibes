package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.ShieldsOfVelisVel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinHarbinger.class, GoldmeadowStalwart.class, HillcomberGiant.class, Plains.class, Island.class,
        ShieldsOfVelisVel.class})
class KithkinHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Kithkin Harbinger creates a may prompt")
    void resolvingCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Kithkin Harbinger");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability only offers Kithkin cards")
    void acceptingMayOffersOnlyKithkin() {
        setupAndCast();
        setupLibraryWithKithkin();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.getSubtypes().contains(CardSubtype.KITHKIN));
    }

    @Test
    @DisplayName("Choosing a Kithkin card puts it on top of the library")
    void choosingKithkinPutsItOnTop() {
        setupAndCast();
        setupLibraryWithKithkin();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).isNotEmpty();
        assertThat(deck.getFirst().getName()).isEqualTo(chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability with no Kithkin finds no card")
    void acceptingMayWithNoKithkinFindsNoCard() {
        setupAndCast();
        setupLibraryWithoutKithkin();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Hillcomber Giant", "Plains", "Island");
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupAndCast();
        setupLibraryWithKithkin();

        harness.passBothPriorities();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A noncreature card with changeling can be found and is revealed")
    void findsNoncreatureChangeling() {
        setupAndCast();
        Card changeling = new ShieldsOfVelisVel();
        Card giant = new HillcomberGiant();
        harness.setLibrary(player1, List.of(giant, changeling));
        Card opposingKithkin = new GoldmeadowStalwart();
        harness.setLibrary(player2, List.of(opposingKithkin));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(changeling);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(changeling, giant);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingKithkin);
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("reveals Shields of Velis Vel", "on top"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search can find nothing even when a Kithkin is available")
    void canFailToFindAvailableKithkin() {
        setupAndCast();
        Card kithkin = new GoldmeadowStalwart();
        Card giant = new HillcomberGiant();
        harness.setLibrary(player1, List.of(kithkin, giant));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(kithkin, giant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the search with an empty library completes the ability")
    void emptyLibraryCompletesSearch() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Kithkin Harbinger");
    }

    private void setupAndCast() {
        harness.castFromHand(player1, new KithkinHarbinger(), "{2}{W}");
    }

    private void setupLibraryWithKithkin() {
        harness.setLibrary(player1, List.of(new GoldmeadowStalwart(), new HillcomberGiant(), new Plains(), new Island()));
    }

    private void setupLibraryWithoutKithkin() {
        harness.setLibrary(player1, List.of(new HillcomberGiant(), new Plains(), new Island()));
    }
}
