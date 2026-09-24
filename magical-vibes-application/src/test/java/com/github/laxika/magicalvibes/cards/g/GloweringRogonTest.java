package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.d.DrippingDead;
import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloweringRogon.class, EnormousBaloth.class, DrippingDead.class, AvianChangeling.class})
class GloweringRogonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one counter for each Beast card revealed from your hand")
    void entersWithCounterForEachBeastCard() {
        GloweringRogon card = new GloweringRogon();
        EnormousBaloth firstBeast = new EnormousBaloth();
        EnormousBaloth secondBeast = new EnormousBaloth();
        harness.setHand(player1, List.of(
                card, firstBeast, secondBeast, new DrippingDead()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBeast.getId(), secondBeast.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBeast.getId(), secondBeast.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, card.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only Beast cards in its controller's hand")
    void ignoresOtherHandsAndNonmatchingCards() {
        GloweringRogon card = new GloweringRogon();
        harness.setHand(player1, List.of(card, new DrippingDead()));
        harness.setHand(player2, List.of(new EnormousBaloth(), new EnormousBaloth()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, card.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("May reveal only some of the Beast cards for amplify")
    void choosesSubsetOfBeastCardsForAmplify() {
        GloweringRogon card = new GloweringRogon();
        EnormousBaloth firstBeast = new EnormousBaloth();
        EnormousBaloth secondBeast = new EnormousBaloth();
        harness.setHand(player1, List.of(card, firstBeast, secondBeast, new DrippingDead()));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBeast.getId(), secondBeast.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBeast.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, card.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("May reveal no Beast cards for amplify")
    void choosesNoBeastCardsForAmplify() {
        GloweringRogon card = new GloweringRogon();
        EnormousBaloth firstBeast = new EnormousBaloth();
        EnormousBaloth secondBeast = new EnormousBaloth();
        harness.setHand(player1, List.of(card, firstBeast, secondBeast));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBeast.getId(), secondBeast.getId());

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, card.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Counts a Changeling card as a Beast card for amplify")
    void countsChangelingAsBeast() {
        GloweringRogon card = new GloweringRogon();
        EnormousBaloth beast = new EnormousBaloth();
        AvianChangeling changeling = new AvianChangeling();
        harness.setHand(player1, List.of(card, beast, changeling));
        payMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(beast.getId(), changeling.getId());

        harness.handleMultipleCardsChosen(player1, List.of(beast.getId(), changeling.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, card.getName()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    private void payMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
