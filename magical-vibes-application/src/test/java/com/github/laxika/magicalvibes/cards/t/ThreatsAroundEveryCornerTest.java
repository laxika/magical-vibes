package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThreatsAroundEveryCorner.class, Forest.class, GrizzlyBears.class})
class ThreatsAroundEveryCornerTest extends BaseCardTest {

    @Test
    void manifestsDreadAndSearchesForABasicLandWhenTheManifestedPermanentEnters() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        Card searchedLand = new Forest();
        harness.setHand(player1, List.of(new ThreatsAroundEveryCorner()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard, searchedLand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction
                .activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(searchedLand);

        harness.handleCardChosen(player1, 0);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard()).isEqualTo(manifestedCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(searchedLand) && permanent.isTapped());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
