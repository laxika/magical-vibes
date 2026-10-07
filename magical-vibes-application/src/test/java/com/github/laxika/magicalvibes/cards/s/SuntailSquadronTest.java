package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuntailSquadron.class, SuntailHawk.class, GrizzlyBears.class})
class SuntailSquadronTest extends BaseCardTest {

    @Test
    void fillsAnEmptyHandWithDistinctOwnedHawks() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player2, new SuntailSquadron(), "{2}{W}{W}");
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player2.getId());
        assertThat(hand).hasSize(7);
        assertThat(hand).allSatisfy(card -> {
            assertThat(card.getName()).isEqualTo("Suntail Hawk");
            assertThat(card.getOwnerId()).isEqualTo(player2.getId());
        });
        assertThat(hand).extracting(Card::getId).doesNotHaveDuplicates();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Suntail Squadron");
    }

    @ParameterizedTest
    @ValueSource(ints = {7, 8})
    void stillConjuresOneHawkWhenHandIsAlreadyFull(int cardsAfterCasting) {
        List<Card> startingHand = new ArrayList<>();
        startingHand.add(new SuntailSquadron());
        for (int i = 0; i < cardsAfterCasting; i++) {
            startingHand.add(new GrizzlyBears());
        }
        harness.setHand(player1, startingHand);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(cardsAfterCasting + 1);
        assertThat(hand).filteredOn(card -> card.getName().equals("Suntail Hawk")).hasSize(1);
        assertThat(hand).filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(cardsAfterCasting);
    }

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
