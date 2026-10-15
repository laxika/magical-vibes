package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenInsight.class, GrizzlyBears.class, HillGiant.class, Island.class, Opt.class, Shock.class})
class SuddenInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each different mana value among nonland cards in its controller's graveyard")
    void drawsForEachDistinctManaValueInOwnGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new Opt();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Shock(), new Opt(), new HillGiant(), new Island()));
        harness.setHand(player1, List.of(new SuddenInsight()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    @DisplayName("Counts only distinct mana values in the caster's graveyard")
    void ignoresOpponentGraveyardAndDuplicateValues() {
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setLibrary(player1, List.of(first, second));
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Island()));
        harness.setGraveyard(player2, List.of(new HillGiant(), new Shock()));
        harness.setHand(player1, List.of(new SuddenInsight()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("An empty graveyard draws nothing and the resolving spell does not count itself")
    void emptyGraveyardDoesNotCountResolvingSpell() {
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new SuddenInsight()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Sudden Insight");
    }

    @Test
    @DisplayName("A graveyard containing only lands draws nothing")
    void landOnlyGraveyardDrawsNothing() {
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setGraveyard(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new SuddenInsight()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Counts mana values when the spell resolves rather than when it is cast")
    void countsGraveyardAtResolution() {
        Card first = new Island();
        Card second = new Island();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SuddenInsight()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }
}
