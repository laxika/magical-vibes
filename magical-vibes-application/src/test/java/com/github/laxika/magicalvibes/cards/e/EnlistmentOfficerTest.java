package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoldierOfThePantheon;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnlistmentOfficer.class, GrizzlyBears.class, Plains.class, Shock.class,
        SoldierOfThePantheon.class, WoodlandChangeling.class})
class EnlistmentOfficerTest extends BaseCardTest {

    private static Card createNoncreatureSoldierCard() {
        Card card = new Card();
        card.setName("Soldier's Training");
        card.setType(CardType.SORCERY);
        card.setSubtypes(List.of(CardSubtype.SOLDIER));
        return card;
    }

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castOfficer() {
        harness.setHand(player1, List.of(new EnlistmentOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soldier cards among the top four go to hand and the rest go to the bottom")
    void soldiersGoToHand() {
        Card soldier1 = new SoldierOfThePantheon();
        Card soldier2 = new SoldierOfThePantheon();
        Card bear = new GrizzlyBears();
        Card plains = new Plains();
        Card shock = new Shock();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(soldier1, soldier2, bear, plains, shock));

        castOfficer();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(soldier1, soldier2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear, plains);
        assertThat(deck).contains(bear, plains, shock);
    }

    @Test
    @DisplayName("Only the top four cards are revealed")
    void onlyTopFourAreRevealed() {
        Card plains1 = new Plains();
        Card plains2 = new Plains();
        Card plains3 = new Plains();
        Card plains4 = new Plains();
        Card deepSoldier = new SoldierOfThePantheon();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(plains1, plains2, plains3, plains4, deepSoldier));

        castOfficer();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepSoldier);
        assertThat(deck).contains(deepSoldier);
    }

    @Test
    @DisplayName("Changeling and noncreature Soldier cards count as Soldier cards")
    void changelingAndNoncreatureSoldierCardsGoToHand() {
        Card changeling = new WoodlandChangeling();
        Card noncreatureSoldier = createNoncreatureSoldierCard();
        Card shock = new Shock();

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(changeling, noncreatureSoldier, shock));

        castOfficer();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling, noncreatureSoldier);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock);
    }
}
