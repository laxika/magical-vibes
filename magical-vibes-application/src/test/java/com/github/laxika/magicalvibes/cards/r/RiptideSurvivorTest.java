package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvenLiberator;
import com.github.laxika.magicalvibes.cards.s.SparkSpray;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptideSurvivor.class, AvenLiberator.class, SparkSpray.class})
class RiptideSurvivorTest extends BaseCardTest {

    @Test
    void turningFaceUpDiscardsTwoCardsThenDrawsThreeCards() {
        AvenLiberator firstDraw = new AvenLiberator();
        AvenLiberator secondDraw = new AvenLiberator();
        AvenLiberator thirdDraw = new AvenLiberator();
        SparkSpray firstDiscard = new SparkSpray();
        SparkSpray secondDiscard = new SparkSpray();
        RiptideSurvivor card = new RiptideSurvivor();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(card, firstDiscard, secondDiscard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent survivor = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(survivor.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpWithOneOtherCardDiscardsItThenDrawsThreeCards() {
        AvenLiberator firstDraw = new AvenLiberator();
        AvenLiberator secondDraw = new AvenLiberator();
        AvenLiberator thirdDraw = new AvenLiberator();
        SparkSpray discard = new SparkSpray();
        RiptideSurvivor card = new RiptideSurvivor();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(card, discard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent survivor = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
        assertThat(survivor.isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpWithEmptyHandStillDrawsThreeCards() {
        AvenLiberator firstDraw = new AvenLiberator();
        AvenLiberator secondDraw = new AvenLiberator();
        AvenLiberator thirdDraw = new AvenLiberator();
        RiptideSurvivor card = new RiptideSurvivor();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent survivor = findPermanentForCard(card);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(survivor));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(survivor.isFaceDown()).isFalse();
    }

    private Permanent findPermanentForCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
