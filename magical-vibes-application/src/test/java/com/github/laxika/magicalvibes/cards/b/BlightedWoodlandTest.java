package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightedWoodland.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class BlightedWoodlandTest extends BaseCardTest {

    @Test
    @DisplayName("It taps for colorless mana")
    void tapsForColorless() {
        harness.addToBattlefield(player1, new BlightedWoodland());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("It searches for up to two basic lands onto the battlefield tapped")
    void searchesForBasicLands() {
        harness.addToBattlefield(player1, new BlightedWoodland());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Blighted Woodland");
    }

    @Test
    @DisplayName("The second land search can be declined")
    void canDeclineSecondLand() {
        harness.addToBattlefield(player1, new BlightedWoodland());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .singleElement()
                .matches(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Blighted Woodland");
    }

    @Test
    @DisplayName("The search can find zero lands even when basic lands are available")
    void canDeclineAllLands() {
        harness.addToBattlefield(player1, new BlightedWoodland());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Blighted Woodland");
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before the search resolves, even with an empty library")
    void paysCostsBeforeResolutionWithEmptyLibrary() {
        harness.addToBattlefield(player1, new BlightedWoodland());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Blighted Woodland");
        harness.assertInGraveyard(player1, "Blighted Woodland");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only one available basic land enters tapped; nonbasic lands remain in the library")
    void findsOnlyAvailableBasicLand() {
        harness.addToBattlefield(player1, new BlightedWoodland());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest(), new BlightedWoodland()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .matches(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement().isInstanceOf(BlightedWoodland.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
