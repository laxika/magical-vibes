package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodhuskRitualist.class, GrizzlyBears.class, HillGiant.class})
class BloodhuskRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("Without multikicker, it does not make the opponent discard")
    void withoutMultikickerNoDiscard() {
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("It makes the opponent discard one card for each multikicker payment")
    void discardsForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{B}", "{B}"), false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With one kick, the opponent chooses which card to discard")
    void opponentChoosesCardAndKeepsTheRest() {
        BloodhuskRitualist kept = new BloodhuskRitualist();
        BloodhuskRitualist discarded = new BloodhuskRitualist();
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.setHand(player2, List.of(kept, discarded));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{B}"), false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("More kicks than cards in hand discard the entire hand without waiting for more choices")
    void discardsOnlyAvailableCards() {
        BloodhuskRitualist discarded = new BloodhuskRitualist();
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.setHand(player2, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{B}", "{B}"), false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A kicked Ritualist can target an opponent with an empty hand")
    void emptyHandNeedsNoDiscardChoice() {
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                List.of("{B}"), false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloodhusk Ritualist");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ETB trigger cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new BloodhuskRitualist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
