package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerchantScroll.class, Boomerang.class, Shock.class, AirElemental.class, GrizzlyBears.class, PsychogenicProbe.class})
class MerchantScrollTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving presents only blue instant cards for the search")
    void resolvingPresentsOnlyBlueInstants() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c.hasType(CardType.INSTANT) && c.getColors().contains(CardColor.BLUE));
    }

    @Test
    @DisplayName("Chosen blue instant goes to hand, is revealed, and library is shuffled")
    void chosenBlueInstantGoesToHand() {
        setupAndCast();
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.hasType(CardType.INSTANT) && c.getColors().contains(CardColor.BLUE));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No prompt is created when the library holds no blue instant")
    void noBlueInstantInLibrary() {
        setupAndCast();

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new Shock(), new AirElemental(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("finds no"));
    }

    @Test
    @DisplayName("Empty library still triggers abilities that trigger when a library is shuffled")
    void emptyLibraryStillTriggersShuffleAbilities() {
        setupAndCast();
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A blue sorcery is excluded while every blue instant remains selectable")
    void excludesBlueSorceriesAndOffersAllBlueInstants() {
        setupAndCast();
        Boomerang first = new Boomerang();
        Boomerang second = new Boomerang();
        harness.setLibrary(player1, List.of(first, new MerchantScroll(), new Shock(), second));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(first, second);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Merchant Scroll");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding a blue instant reveals it and triggers a shuffle ability exactly once")
    void successfulSearchRevealsCardAndTriggersShuffle() {
        setupAndCast();
        harness.setLibrary(player1, List.of(new Boomerang(), new GrizzlyBears()));
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Boomerang");
        harness.assertLife(player1, 18);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals") && entry.contains("Boomerang"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search may find nothing even with a blue instant present and still shuffles")
    void canFailToFindWithMatchingCardPresent() {
        setupAndCast();
        Boomerang available = new Boomerang();
        harness.setLibrary(player1, List.of(available));
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(available);
        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player1, "Merchant Scroll");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
    private void setupAndCast() {
        harness.castFromHand(player1, new MerchantScroll(), "{1}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Boomerang(), new Shock(), new AirElemental(), new GrizzlyBears()));
    }
}
