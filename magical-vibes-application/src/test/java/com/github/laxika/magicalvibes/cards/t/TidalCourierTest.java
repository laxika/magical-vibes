package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TidalCourier.class, MerfolkOfThePearlTrident.class, Plains.class, Shock.class})
class TidalCourierTest extends BaseCardTest {

    private static Card createNoncreatureMerfolk() {
        Card card = new Card();
        card.setName("Merfolk Research");
        card.setType(CardType.SORCERY);
        card.setSubtypes(List.of(CardSubtype.MERFOLK));
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castCourier() {
        harness.setHand(player1, List.of(new TidalCourier()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Merfolk cards among the top four go to hand and the rest go to the bottom")
    void merfolkCardsGoToHand() {
        Card merfolk1 = new MerfolkOfThePearlTrident();
        Card plains = new Plains();
        Card merfolk2 = new MerfolkOfThePearlTrident();
        Card shock = new Shock();
        Card deepMerfolk = new MerfolkOfThePearlTrident();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(merfolk1, plains, merfolk2, shock, deepMerfolk));

        castCourier();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(merfolk1, merfolk2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains, shock, deepMerfolk);
        assertThat(deck).contains(plains, shock, deepMerfolk);
    }

    @Test
    @DisplayName("Noncreature Merfolk cards also go to hand")
    void noncreatureMerfolkCardsGoToHand() {
        Card merfolk = createNoncreatureMerfolk();
        Card shock = new Shock();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(merfolk, shock));

        castCourier();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(merfolk);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TidalCourier());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(courier.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
