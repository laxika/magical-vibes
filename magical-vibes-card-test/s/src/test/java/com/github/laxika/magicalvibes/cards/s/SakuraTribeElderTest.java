package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakuraTribeElder.class, Plains.class, Forest.class, Island.class,
        HumbleBudoka.class, EiganjoCastle.class})
class SakuraTribeElderTest extends BaseCardTest {

    @Test
    @DisplayName("Sakura-Tribe Elder is sacrificed as part of the cost, with no mana paid")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new SakuraTribeElder());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Sakura-Tribe Elder");
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("Activating the ability searches for a basic land that enters tapped")
    void searchPutsBasicLandTappedOntoBattlefield() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        setupLibrary();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With no basic lands in library, no land enters the battlefield")
    void failToFindNoBasicLand() {
        harness.addToBattlefield(player1, new SakuraTribeElder());

        harness.setLibrary(player1, List.of(new HumbleBudoka(), new HumbleBudoka()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → no basic land to find

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Nonbasic lands are not eligible for the search")
    void ignoresNonbasicLands() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.setLibrary(player1, List.of(new EiganjoCastle(), new HumbleBudoka()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new HumbleBudoka()));
    }

    @Test
    @DisplayName("The controller can fail to find even when a basic land is available")
    void canDeclineFindingAvailableLand() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("An empty library does not prevent paying the sacrifice cost")
    void emptyLibraryStillSacrificesElder() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }

    @Test
    @DisplayName("A tapped Elder can activate and searches only its controller's library")
    void tappedElderSearchesControllersLibrary() {
        harness.addToBattlefieldAndReturn(player1, new SakuraTribeElder()).tap();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(island));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
    }
}
