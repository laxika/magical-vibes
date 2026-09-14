package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({MyriadLandscape.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class MyriadLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("It enters tapped and taps for colorless mana")
    void entersTappedAndTapsForColorless() {
        harness.setHand(player1, List.of(new MyriadLandscape()));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof MyriadLandscape)
                .singleElement()
                .matches(Permanent::isTapped);

        Permanent landscape = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof MyriadLandscape)
                .findFirst()
                .orElseThrow();
        landscape.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("It searches for up to two basic lands sharing a land type")
    void searchesForSharingBasicLands() {
        harness.addToBattlefield(player1, new MyriadLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(4)
                .allMatch(card -> card instanceof Forest || card instanceof Island || card instanceof Plains);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .isInstanceOf(Forest.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof Island || p.getCard() instanceof Plains);
        harness.assertInGraveyard(player1, "Myriad Landscape");
    }

    @Test
    @DisplayName("The second land search can be declined")
    void canDeclineSecondLand() {
        harness.addToBattlefield(player1, new MyriadLandscape());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Plains(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest)
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Myriad Landscape");
    }
}
