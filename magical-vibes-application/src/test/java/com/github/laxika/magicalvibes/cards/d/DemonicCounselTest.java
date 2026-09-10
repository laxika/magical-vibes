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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

        gs.handleInteractionAnswer(gameData, player1, new InteractionAnswer.LibraryCardChosen(cardIndex));

        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Millstone");
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
