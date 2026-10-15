package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.handleCardChosen(player1, 0);

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
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search requires one mana and does not sacrifice the land when payment fails")
    void searchRequiresMana() {
        harness.addToBattlefield(player1, new ShireTerrace());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shire Terrace");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Shire Terrace cannot pay for its own search by tapping for mana")
    void cannotSearchAfterTappingForMana() {
        harness.addToBattlefield(player1, new ShireTerrace());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shire Terrace");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void emptyLibraryFinishesNormally() {
        harness.setLibrary(player1, List.of());
        activateSearch();

        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shire Terrace");
    }

    @Test
    @DisplayName("Search excludes nonbasic lands and puts exactly one basic land onto the battlefield")
    void excludesNonbasicLandsAndFindsExactlyOne() {
        Plains plains = new Plains();
        Island island = new Island();
        ShireTerrace nonbasicLand = new ShireTerrace();
        harness.setLibrary(player1, List.of(nonbasicLand, plains, island));
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = harness.getGameData()
                .interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, island);
        harness.handleCardChosen(player1, 0);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(plains.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(harness.getGameData().playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonbasicLand, island);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isZero();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new ShireTerrace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
