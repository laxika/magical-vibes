package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BaralChiefOfCompliance;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Unravel.class, BaralChiefOfCompliance.class, Divination.class, Forest.class, HeartlessSummoning.class, StinkweedImp.class})
class UnravelTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an underpaid spell and draws a card")
    void countersUnderpaidSpellAndDraws() {
        harness.addToBattlefield(player1, new BaralChiefOfCompliance());
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new Unravel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination.getId());

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player2.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Does not draw when the target spell's full mana value was spent")
    void doesNotDrawWhenFullManaWasSpent() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Forest drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new Unravel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination.getId());

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(drawnCard);
    }

    @Test
    @DisplayName("Can dredge the underpaid spell after countering it")
    void canDredgeTheCounteredSpell() {
        harness.addToBattlefield(player1, new HeartlessSummoning());
        StinkweedImp imp = new StinkweedImp();
        harness.setHand(player1, List.of(imp, new Unravel()));
        List<Card> milled = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, milled);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, imp.getId());

        harness.assertInGraveyard(player1, "Stinkweed Imp");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(imp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(milled).doesNotContain(imp);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }
}
