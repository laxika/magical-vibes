package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircadianStruggle.class, AirElemental.class, GrizzlyBears.class, Shock.class})
class CircadianStruggleTest extends BaseCardTest {

    @Test
    void seeksOneMatchingCardPerControlledColorAndReducesEachCostPerColor() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        Card greenCard = new GrizzlyBears();
        Card blueCard = new AirElemental();
        Card redCard = new Shock();
        harness.setLibrary(player1, List.of(greenCard, blueCard, redCard));
        harness.setHand(player1, List.of(new CircadianStruggle()));
        addCircadianMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(greenCard, blueCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(redCard);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(greenCard));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(blueCard));
        harness.passBothPriorities();
    }

    @Test
    void seeksNothingWhenNoControlledPermanentHasAColor() {
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new CircadianStruggle()));
        addCircadianMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addCircadianMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
