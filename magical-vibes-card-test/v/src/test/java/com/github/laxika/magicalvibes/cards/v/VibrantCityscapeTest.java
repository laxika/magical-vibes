package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AuntMay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VibrantCityscape.class, Forest.class, AuntMay.class, Island.class, Plains.class})
class VibrantCityscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Vibrant Cityscape sacrifices it and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new VibrantCityscape());

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Vibrant Cityscape");
        harness.assertInGraveyard(player1, "Vibrant Cityscape");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Searching offers only basic lands and puts the chosen land onto the battlefield tapped")
    void searchesForBasicLandToBattlefieldTapped() {
        harness.addToBattlefield(player1, new VibrantCityscape());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new AuntMay()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(card -> card instanceof AuntMay);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("A restricted search may fail to find even when a basic land is available")
    void canFailToFindAnAvailableBasicLand() {
        harness.addToBattlefield(player1, new VibrantCityscape());
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vibrant Cityscape");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A nonbasic land cannot be found and remains in the library")
    void cannotFindNonbasicLand() {
        harness.addToBattlefield(player1, new VibrantCityscape());
        VibrantCityscape nonbasicLand = new VibrantCityscape();
        harness.setLibrary(player1, List.of(nonbasicLand));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vibrant Cityscape");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library resolves without putting a land onto the battlefield")
    void resolvesWithEmptyLibrary() {
        harness.addToBattlefield(player1, new VibrantCityscape());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vibrant Cityscape");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Vibrant Cityscape cannot pay the activation cost")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new VibrantCityscape());
        findPermanent(player1, "Vibrant Cityscape").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vibrant Cityscape");
        harness.assertNotInGraveyard(player1, "Vibrant Cityscape");
        assertThat(gd.stack).isEmpty();
    }
}
