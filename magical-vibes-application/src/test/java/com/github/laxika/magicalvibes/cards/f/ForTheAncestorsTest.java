package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForTheAncestors.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        Tarfire.class, WoodlandChangeling.class})
class ForTheAncestorsTest extends BaseCardTest {

    @Test
    @DisplayName("chooses a creature type and puts any selected matching cards into hand")
    void choosesTypeAndPutsMatchingCardsIntoHand() {
        GrizzlyBears bear = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Tarfire goblinCard = new Tarfire();
        WoodlandChangeling changeling = new WoodlandChangeling();
        Shock shock = new Shock();
        Card tail = new HillGiant();
        setTopCards(List.of(goblinCard, giant, changeling, shock, bear, new GrizzlyBears(), tail));

        castForTheAncestors();
        harness.handleListChoice(player1, "GOBLIN");
        harness.handleMultipleCardsChosen(player1, List.of(goblinCard.getId(), changeling.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(goblinCard, changeling);
        assertThat(gd.playerDecks.get(player1.getId()))
                .contains(tail, giant, shock, bear)
                .doesNotContain(goblinCard, changeling);
    }

    @Test
    @DisplayName("may keep no matching cards")
    void mayKeepNoMatchingCards() {
        GrizzlyBears bear = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        HillGiant giant2 = new HillGiant();
        GrizzlyBears bear2 = new GrizzlyBears();
        setTopCards(List.of(bear, giant, shock1, shock2, giant2, bear2));

        castForTheAncestors();
        harness.handleListChoice(player1, "BEAR");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(bear, giant, shock1, shock2, giant2, bear2);
    }

    @Test
    @DisplayName("only looks at the top six cards")
    void onlyLooksAtTopSixCards() {
        HillGiant deepGiant = new HillGiant();
        setTopCards(List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), deepGiant));

        castForTheAncestors();
        harness.handleListChoice(player1, "GIANT");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(deepGiant);
    }

    private void castForTheAncestors() {
        harness.setHand(player1, List.of(new ForTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
