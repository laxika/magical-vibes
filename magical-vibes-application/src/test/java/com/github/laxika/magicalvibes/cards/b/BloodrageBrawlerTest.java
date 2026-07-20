package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodrageBrawlerTest extends BaseCardTest {

    private void castBrawler() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger onto stack
        harness.passBothPriorities(); // resolve ETB discard trigger
    }

    @Test
    @DisplayName("When Bloodrage Brawler enters, its controller discards a card")
    void entersPromptsControllerDiscard() {
        harness.setHand(player1, List.of(new BloodrageBrawler(), new GrizzlyBears()));

        castBrawler();

        // Controller must choose a card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0); // only Grizzly Bears remains in hand

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bloodrage Brawler"));
    }
}
