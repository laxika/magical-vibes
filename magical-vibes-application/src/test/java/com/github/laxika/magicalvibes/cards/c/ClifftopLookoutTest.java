package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClifftopLookout.class, Forest.class, Shock.class})
class ClifftopLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Enters a land tapped and puts preceding revealed cards on the library bottom")
    void entersLandTappedAndBottomsRevealedCards() {
        Card shock = new Shock();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(shock, forest));
        castClifftopLookout();

        Permanent enteredLand = findPermanent(player1, forest.getName());
        assertThat(enteredLand.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Reorders all revealed cards when no land is found")
    void reordersAllRevealedCardsWhenNoLandIsFound() {
        Card first = new Shock();
        Card second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        castClifftopLookout();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> revealed = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(revealed.indexOf(second), revealed.indexOf(first))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == first || permanent.getCard() == second);
    }

    private void castClifftopLookout() {
        harness.setHand(player1, List.of(new ClifftopLookout()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
