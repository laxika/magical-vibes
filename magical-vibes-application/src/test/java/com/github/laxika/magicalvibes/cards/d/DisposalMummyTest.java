package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BitterbowSharpshooters;
import com.github.laxika.magicalvibes.cards.h.HourOfDevastation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisposalMummy.class, BitterbowSharpshooters.class, HourOfDevastation.class})
class DisposalMummyTest extends BaseCardTest {

    /** Casts Disposal Mummy and resolves the creature spell so its ETB sets up graveyard targeting. */
    private void castDisposalMummy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DisposalMummy(), "{2}{W}");
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB exiles a targeted card from an opponent's graveyard")
    void etbExilesOpponentGraveyardCard() {
        Card bears = new BitterbowSharpshooters();
        harness.setGraveyard(player2, List.of(bears));

        castDisposalMummy();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability

        harness.assertNotInGraveyard(player2, "Bitterbow Sharpshooters");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("A card in the controller's own graveyard is not a legal target")
    void ownGraveyardCardNotTargetable() {
        Card bears = new BitterbowSharpshooters();
        harness.setGraveyard(player1, List.of(bears));

        castDisposalMummy();

        // Only the controller's graveyard has a card → no opponent target, no choice presented.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();

        harness.assertInGraveyard(player1, "Bitterbow Sharpshooters");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Empty opponent graveyard produces no target choice")
    void emptyOpponentGraveyardNoChoice() {
        castDisposalMummy();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotDeclineMandatoryExileWhenLegalTargetExists() {
        Card target = new BitterbowSharpshooters();
        harness.setGraveyard(player2, List.of(target));

        castDisposalMummy();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void exilesOnlyChosenCardAndCanTargetNoncreatureCard() {
        Card target = new HourOfDevastation();
        Card other = new BitterbowSharpshooters();
        Card own = new BitterbowSharpshooters();
        harness.setGraveyard(player2, List.of(other, target));
        harness.setGraveyard(player1, List.of(own));

        castDisposalMummy();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(own.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(other.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertInGraveyard(player2, "Bitterbow Sharpshooters");
        harness.assertInGraveyard(player1, "Bitterbow Sharpshooters");
    }

    @Test
    void targetLeavingGraveyardDoesNotCauseAnotherCardToBeExiled() {
        Card target = new BitterbowSharpshooters();
        Card other = new HourOfDevastation();
        harness.setGraveyard(player2, List.of(target, other));

        castDisposalMummy();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Bitterbow Sharpshooters");
        harness.assertInGraveyard(player2, "Hour of Devastation");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
