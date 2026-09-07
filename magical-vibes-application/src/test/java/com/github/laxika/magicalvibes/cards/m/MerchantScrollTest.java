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

    private void setupAndCast() {
        harness.castFromHand(player1, new MerchantScroll(), "{1}{U}");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Boomerang(), new Shock(), new AirElemental(), new GrizzlyBears()));
    }
}
