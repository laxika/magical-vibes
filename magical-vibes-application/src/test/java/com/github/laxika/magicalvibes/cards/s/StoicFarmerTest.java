package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicFarmer.class, Plains.class, Forest.class})
class StoicFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a basic Plains into hand when no opponent controls more lands")
    void putsPlainsIntoHandWhenOpponentIsNotAhead() {
        castFarmer();
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        resolveEtb();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB offers a basic Plains onto the battlefield tapped when an opponent controls more lands")
    void putsPlainsOntoBattlefieldTappedWhenOpponentIsAhead() {
        castFarmer();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        resolveEtb();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting(Card::getName).containsExactly("Plains");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(1)
                .allMatch(Permanent::isTapped);
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFarmer() {
        harness.setHand(player1, List.of(new StoicFarmer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void resolveEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }
}
