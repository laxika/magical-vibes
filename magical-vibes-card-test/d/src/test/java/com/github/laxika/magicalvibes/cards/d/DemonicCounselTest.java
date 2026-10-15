package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YawgmothDemon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicCounsel.class, YawgmothDemon.class, GrizzlyBears.class, Forest.class, Shock.class, Millstone.class})
class DemonicCounselTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, offers only Demons")
    void withoutDeliriumOffersOnlyDemons() {
        setupLibrary();
        cast();

        PendingInteraction.LibrarySearch search = librarySearch();

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Yawgmoth Demon");
    }

    @Test
    @DisplayName("With delirium, offers any card")
    void withDeliriumOffersAnyCard() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();

        PendingInteraction.LibrarySearch search = librarySearch();

        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Yawgmoth Demon", "Grizzly Bears", "Forest", "Shock", "Millstone");
    }

    @Test
    @DisplayName("With delirium, can put a non-Demon card into hand")
    void withDeliriumCanPutAnyCardIntoHand() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();

        GameData gameData = harness.getGameData();
        List<Card> offered = librarySearch().params().cards();
        int cardIndex = offered.stream().map(Card::getName).toList().indexOf("Millstone");
        int handBefore = gameData.playerHands.get(player1.getId()).size();

        harness.handleCardChosen(player1, cardIndex);

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Millstone");
    }


    @Test
    void withoutDeliriumRevealsDemonAndPutsItIntoHand() {
        setupLibrary();
        cast();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Yawgmoth Demon");
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .extracting(Card::getName).doesNotContain("Yawgmoth Demon").hasSize(4);
        assertThat(harness.getGameData().gameLog)
                .anyMatch(entry -> entry.plainText().contains("reveals Yawgmoth Demon"));
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void restrictedSearchMayFailToFindEvenWithDemonAvailable() {
        setupLibrary();
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(5);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void deliriumSearchDoesNotRevealChosenCard() {
        setupDeliriumGraveyard();
        setupLibrary();
        cast();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Yawgmoth Demon");
        assertThat(harness.getGameData().gameLog)
                .noneMatch(entry -> entry.plainText().contains("reveals Yawgmoth Demon"));
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    void spellOnStackDoesNotSupplyFourthGraveyardType() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        setupLibrary();
        cast();

        assertThat(librarySearch().params().cards()).extracting(Card::getName)
                .containsExactly("Yawgmoth Demon");
    }

    @Test
    void deliriumGainedAfterCastingIsCheckedAtResolution() {
        setupLibrary();
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        setupDeliriumGraveyard();
        harness.passBothPriorities();

        assertThat(librarySearch().params().cards()).hasSize(5);
    }

    @Test
    void deliriumLostAfterCastingUsesDemonSearch() {
        setupDeliriumGraveyard();
        setupLibrary();
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.passBothPriorities();

        assertThat(librarySearch().params().cards()).extracting(Card::getName)
                .containsExactly("Yawgmoth Demon");
    }

    @Test
    void noDemonInLibraryFinishesWithoutTakingCard() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));
        cast();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(2);
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryWithDeliriumFinishesNormally() {
        setupDeliriumGraveyard();
        harness.setLibrary(player1, List.of());
        cast();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
    }

    private void cast() {
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch librarySearch() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new YawgmothDemon(),
                new GrizzlyBears(),
                new Forest(),
                new Shock(),
                new Millstone()
        ));
    }

    private void setupDeliriumGraveyard() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(),
                new Forest(),
                new Shock(),
                new Millstone()
        ));
    }
}
