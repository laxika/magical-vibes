package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivelyDirge.class, GrizzlyBears.class, HillGiant.class, Plains.class, SerraAngel.class})
class LivelyDirgeTest extends BaseCardTest {

    @Test
    void searchesAnyCardIntoGraveyard() {
        Card searchedCard = new Plains();
        harness.setLibrary(player1, List.of(searchedCard, new GrizzlyBears()));

        cast(new int[]{0}, 3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void returnsUpToTwoCreaturesWithinAggregateManaValue() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card fourManaCreature = new HillGiant();
        Card fiveManaCreature = new SerraAngel();
        Card land = new Plains();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, fourManaCreature, fiveManaCreature, land));

        cast(new int[]{1}, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(firstBear.getId(), secondBear.getId(), fourManaCreature.getId())
                .doesNotContain(fiveManaCreature.getId(), land.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBear.getId(), fourManaCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value 4");

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(firstBear.getId(), secondBear.getId())
                .doesNotContain(fourManaCreature.getId(), fiveManaCreature.getId());
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void mayReturnNoCreatures() {
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        cast(new int[]{1}, 4);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void bothModesReturnSearchedCreatureAndCreatureAlreadyInGraveyard() {
        Card searchedBear = new GrizzlyBears();
        Card buriedBear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(searchedBear, new Plains()));
        harness.setGraveyard(player1, List.of(buriedBear));

        cast(new int[]{1, 0}, 5);
        harness.handleCardChosen(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(searchedBear.getId(), buriedBear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(searchedBear.getId(), buriedBear.getId());
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void mayReturnOnlyOneCreatureWithManaValueFour() {
        Card giant = new HillGiant();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(giant, bear));

        cast(new int[]{1}, 4);
        harness.handleMultipleCardsChosen(player1, List.of(giant.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void secondModeResolvesWithoutEligibleCreaturesAndDoesNotUseOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new Plains(), new SerraAngel()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        cast(new int[]{1}, 4);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player1, "Serra Angel");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void emptyLibraryDoesNotPreventSecondModeFromReturningCreature() {
        Card bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(bear));

        cast(new int[]{0, 1}, 5);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lively Dirge");
    }

    @Test
    void bothModesRequireBothAdditionalManaCosts() {
        harness.setHand(player1, List.of(new LivelyDirge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lively Dirge");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotReturnMoreThanTwoCreatures() {
        Card firstBear = new GrizzlyBears();
        Card secondBear = new GrizzlyBears();
        Card thirdBear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstBear, secondBear, thirdBear));

        cast(new int[]{1}, 4);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBear.getId(), secondBear.getId(), thirdBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many cards selected");

        harness.handleMultipleCardsChosen(player1, List.of(firstBear.getId(), secondBear.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
    }

    private void cast(int[] modes, int totalMana) {
        harness.setHand(player1, List.of(new LivelyDirge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
