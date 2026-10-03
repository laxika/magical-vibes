package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasilicaBellHaunt.class})
class BasilicaBellHauntTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent discards a card and you gain 3 life")
    void entersEachOpponentDiscardsAndControllerGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BasilicaBellHaunt()));
        harness.setHand(player2, List.of(new BasilicaBellHaunt()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("It still gains 3 life when an opponent has no cards to discard")
    void emptyOpponentHandStillGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BasilicaBellHaunt()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Opponent chooses exactly one card while the controller keeps their hand")
    void opponentChoosesOneCardAndControllerKeepsHand() {
        BasilicaBellHaunt keptByController = new BasilicaBellHaunt();
        BasilicaBellHaunt keptByOpponent = new BasilicaBellHaunt();
        BasilicaBellHaunt discarded = new BasilicaBellHaunt();
        harness.setHand(player1, List.of(new BasilicaBellHaunt(), keptByController));
        harness.setHand(player2, List.of(keptByOpponent, discarded));
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent, discarded);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptByController);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Entering without being cast makes the active opponent discard and gains life for its controller")
    void enteringWithoutCastingUsesItsController() {
        BasilicaBellHaunt discarded = new BasilicaBellHaunt();
        BasilicaBellHaunt keptByController = new BasilicaBellHaunt();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(keptByController));
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);

        harness.enterBattlefieldAndReturn(player2, new BasilicaBellHaunt());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByController);
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }
}
