package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StuntedGrowth.class, BalduvianBears.class})
class StuntedGrowthTest extends BaseCardTest {

    private List<Card> targetHand(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new BalduvianBears());
        }
        return cards;
    }

    private void castStuntedGrowth() {
        harness.setHand(player1, List.of(new StuntedGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Asks the targeted player to choose three of their hand cards")
    void promptsTargetPlayer() {
        harness.setHand(player2, targetHand(5));

        castStuntedGrowth();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player2.getId());
                    assertThat(choice.minCount()).isEqualTo(3);
                    assertThat(choice.maxCount()).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("The caster may also be the targeted player")
    void canTargetController() {
        List<Card> hand = targetHand(3);
        List<Card> casterHand = new ArrayList<>();
        casterHand.add(new StuntedGrowth());
        casterHand.addAll(hand);
        harness.setHand(player1, casterHand);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player1.getId());
                    assertThat(choice.minCount()).isEqualTo(3);
                    assertThat(choice.maxCount()).isEqualTo(3);
                });

        harness.handleMultipleCardsChosen(player1, hand.stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chosen cards go on top of the target player's library, first chosen nearest the top")
    void putsChosenCardsOnTop() {
        List<Card> hand = targetHand(5);
        harness.setHand(player2, hand);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        Card oldTop = deck.getFirst();

        castStuntedGrowth();

        harness.handleMultipleCardsChosen(player2,
                List.of(hand.get(0).getId(), hand.get(1).getId(), hand.get(2).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(hand.get(3), hand.get(4));
        assertThat(gd.playerDecks.get(player2.getId()))
                .startsWith(hand.get(0), hand.get(1), hand.get(2), oldTop);
    }

    @Test
    @DisplayName("A smaller hand only offers as many cards as the target player holds")
    void smallHandCapsTheChoice() {
        List<Card> hand = targetHand(2);
        harness.setHand(player2, hand);

        castStuntedGrowth();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class,
                        choice -> {
                            assertThat(choice.minCount()).isEqualTo(2);
                            assertThat(choice.maxCount()).isEqualTo(2);
                        });

        harness.handleMultipleCardsChosen(player2, List.of(hand.get(0).getId(), hand.get(1).getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(hand.get(0), hand.get(1));
    }

    @Test
    @DisplayName("An empty hand resolves with no prompt")
    void emptyHandNoPrompt() {
        harness.setHand(player2, List.of());

        castStuntedGrowth();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
