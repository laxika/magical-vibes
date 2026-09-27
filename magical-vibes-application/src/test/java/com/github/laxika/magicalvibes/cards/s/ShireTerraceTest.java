package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShireTerrace.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class ShireTerraceTest extends BaseCardTest {

    @Test
    @DisplayName("Shire Terrace taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new ShireTerrace());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Activating Shire Terrace's search ability sacrifices it")
    void activatingSearchSacrificesSource() {
        harness.addToBattlefield(player1, new ShireTerrace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Shire Terrace");
        harness.assertInGraveyard(player1, "Shire Terrace");
    }

    @Test
    @DisplayName("Search ability presents only basic lands entering tapped")
    void presentsBasicLandsTapped() {
        activateSearch();
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData()
                .interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped")
    void chosenBasicLandEntersTapped() {
        activateSearch();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND)
                        && permanent.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find a basic land")
    void canFailToFind() {
        activateSearch();
        setupLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new ShireTerrace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
