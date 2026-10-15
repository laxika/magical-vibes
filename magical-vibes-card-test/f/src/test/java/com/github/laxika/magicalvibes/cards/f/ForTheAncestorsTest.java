package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
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

    @Test
    @DisplayName("may select only some matching cards and bottoms the unselected matches")
    void maySelectOnlySomeMatchingCards() {
        GrizzlyBears selected = new GrizzlyBears();
        GrizzlyBears declined = new GrizzlyBears();
        Shock nonmatching = new Shock();
        harness.setLibrary(player1, List.of(selected, declined, nonmatching));

        castForTheAncestors();
        harness.handleListChoice(player1, "BEAR");
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(declined, nonmatching);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("can take every matching card when the library has fewer than six cards")
    void takesAllMatchesFromShortLibrary() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        castForTheAncestors();
        harness.handleListChoice(player1, "BEAR");
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("resolves with an empty library without drawing cards")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        castForTheAncestors();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "For the Ancestors");
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("flashback resolves the selection and exiles the spell")
    void flashbackResolvesAndExilesSpell() {
        ForTheAncestors spell = new ForTheAncestors();
        Tarfire matching = new Tarfire();
        Shock nonmatching = new Shock();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(matching, nonmatching));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleListChoice(player1, "GOBLIN");
        harness.handleMultipleCardsChosen(player1, List.of(matching.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matching);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(spell.getId()));
        assertThat(gd.stack).isEmpty();
    }

    private void castForTheAncestors() {
        harness.setHand(player1, List.of(new ForTheAncestors()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);
    }

    private void setTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
