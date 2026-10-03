package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NefCropEntangler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodrageBrawler.class, NefCropEntangler.class})
class BloodrageBrawlerTest extends BaseCardTest {

    private void castBrawler() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("When Bloodrage Brawler enters, its controller discards a card")
    void entersPromptsControllerDiscard() {
        harness.setHand(player1, List.of(new BloodrageBrawler(), new NefCropEntangler()));

        castBrawler();

        // Controller must choose a card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Nef-Crop Entangler");
    }

    @Test
    @DisplayName("Bloodrage Brawler enters with an empty hand and forces no discard")
    void emptyHandNoDiscard() {
        harness.setHand(player1, List.of(new BloodrageBrawler()));

        castBrawler();

        // Hand is empty after casting the Brawler, so no discard choice is prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        // Brawler is on the battlefield.
        harness.assertOnBattlefield(player1, "Bloodrage Brawler");
    }

    @Test
    @DisplayName("The controller chooses exactly one card and the opponent does not discard")
    void controllerChoosesOneCardFromMultipleCards() {
        BloodrageBrawler retained = new BloodrageBrawler();
        NefCropEntangler discarded = new NefCropEntangler();
        NefCropEntangler opponentCard = new NefCropEntangler();
        harness.setHand(player1, List.of(new BloodrageBrawler(), retained, discarded));
        harness.setHand(player2, List.of(opponentCard));

        castBrawler();
        harness.assertOnBattlefield(player1, "Bloodrage Brawler");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained, discarded);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
