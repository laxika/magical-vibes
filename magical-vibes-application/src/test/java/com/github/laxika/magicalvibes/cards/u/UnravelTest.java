package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BaralChiefOfCompliance;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Unravel.class, BaralChiefOfCompliance.class, Divination.class, Forest.class})
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
        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player2, 0, divination.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(drawnCard);
    }
}
