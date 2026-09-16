package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Everdream.class, Shock.class, GrizzlyBears.class})
class EverdreamTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when cast")
    void drawsCardWhenCast() {
        harness.setHand(player1, List.of(new Everdream()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Splices onto an instant, draws a card, and stays in hand")
    void splicesOntoInstant() {
        Card shock = new Shock();
        Everdream everdream = new Everdream();
        harness.setHand(player1, List.of(shock, everdream));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Everdream");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
