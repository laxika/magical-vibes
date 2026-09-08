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

    private void cast(int[] modes, int totalMana) {
        harness.setHand(player1, List.of(new LivelyDirge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }
}
