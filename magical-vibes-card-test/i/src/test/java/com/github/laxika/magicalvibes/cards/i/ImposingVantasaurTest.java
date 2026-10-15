package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImposingVantasaur.class})
class ImposingVantasaurTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling {1} discards Imposing Vantasaur and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ImposingVantasaur()));
        harness.setLibrary(player1, List.of(new ImposingVantasaur()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Imposing Vantasaur");
        harness.assertInHand(player1, "Imposing Vantasaur");
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only when the ability resolves")
    void cyclingDiscardsBeforeResolution() {
        var cycledCard = new ImposingVantasaur();
        var drawnCard = new ImposingVantasaur();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling without mana leaves the source card in hand")
    void cyclingRequiresOneMana() {
        var card = new ImposingVantasaur();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Imposing Vantasaur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling can be activated during the opponent's turn")
    void cyclingDuringOpponentsTurn() {
        var cycledCard = new ImposingVantasaur();
        var drawnCard = new ImposingVantasaur();
        harness.setHand(player1, List.of(cycledCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycledCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance allows Imposing Vantasaur to attack without tapping")
    void attackingDoesNotTap() {
        var vantasaur = addCreatureReady(player1, new ImposingVantasaur());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(vantasaur.isAttacking()).isTrue();
        assertThat(vantasaur.isTapped()).isFalse();
    }
}
