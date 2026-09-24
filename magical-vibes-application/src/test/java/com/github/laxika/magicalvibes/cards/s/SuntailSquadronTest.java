package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuntailSquadron.class, SuntailHawk.class, GrizzlyBears.class})
class SuntailSquadronTest extends BaseCardTest {

    @Test
    void conjuresHawksUntilTheControllerHasSevenCards() {
        harness.setHand(player1, List.of(new SuntailSquadron(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(7);
        assertThat(hand).filteredOn(card -> card.getName().equals("Suntail Hawk")).hasSize(6);
        assertThat(hand).filteredOn(card -> card.getName().equals("Grizzly Bears")).hasSize(1);
    }

    @Test
    void stopsAfterTheFirstHawkWhenHandAlreadyHasSixCardsAfterCasting() {
        harness.setHand(player1, List.of(
                new SuntailSquadron(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(7);
        assertThat(hand).filteredOn(card -> card.getName().equals("Suntail Hawk")).hasSize(1);
    }
}
