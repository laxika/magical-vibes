package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.v.VodalianMystic;
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

@CardUsed({TidalCourier.class, VodalianMystic.class, Index.class, Dodecapod.class})
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
        harness.castFromHand(player1, new TidalCourier(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Merfolk cards among the top four go to hand and the rest go to the bottom")
    void merfolkCardsGoToHand() {
        Card merfolk1 = new TidalCourier();
        Card nonMerfolk1 = new Index();
        Card merfolk2 = new VodalianMystic();
        Card nonMerfolk2 = new Dodecapod();
        Card deepMerfolk = new VodalianMystic();
        harness.setLibrary(player1, List.of(merfolk1, nonMerfolk1, merfolk2, nonMerfolk2, deepMerfolk));

        castCourier();
        finishAnyReorder();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(merfolk1, merfolk2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonMerfolk1, nonMerfolk2, deepMerfolk);
        assertThat(deck).contains(nonMerfolk1, nonMerfolk2, deepMerfolk);
    }

    @Test
    @DisplayName("Noncreature Merfolk cards also go to hand")
    void noncreatureMerfolkCardsGoToHand() {
        Card merfolk = createNoncreatureMerfolk();
        Card nonMerfolk = new Index();
        harness.setLibrary(player1, List.of(merfolk, nonMerfolk));

        castCourier();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(merfolk);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonMerfolk);
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());

        castCourier();

        harness.assertOnBattlefield(player1, "Tidal Courier");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activated ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TidalCourier());
        Permanent dodecapod = harness.addToBattlefieldAndReturn(player1, new Dodecapod());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(courier.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(dodecapod.hasKeyword(Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(courier.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
