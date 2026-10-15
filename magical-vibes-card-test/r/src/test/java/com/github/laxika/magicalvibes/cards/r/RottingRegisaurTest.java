package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RottingRegisaur.class, GrizzlyBears.class})
class RottingRegisaurTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of its controller's upkeep, its controller discards a card")
    void discardsAtControllerUpkeep() {
        harness.addToBattlefield(player1, new RottingRegisaur());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new RottingRegisaur());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The upkeep ability triggers with an empty hand and resolves without a discard")
    void emptyHandDoesNotPreventTrigger() {
        harness.addToBattlefield(player1, new RottingRegisaur());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Rotting Regisaur");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses exactly one card and the opponent does not discard")
    void controllerChoosesOneCard() {
        harness.addToBattlefield(player1, new RottingRegisaur());
        RottingRegisaur retained = new RottingRegisaur();
        RottingRegisaur discarded = new RottingRegisaur();
        RottingRegisaur opponentsCard = new RottingRegisaur();
        harness.setHand(player1, List.of(retained, discarded));
        harness.setHand(player2, List.of(opponentsCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each Regisaur triggers separately, even when the first discard empties the hand")
    void multipleCopiesResolveWithOnlyOneCardInHand() {
        harness.addToBattlefield(player1, new RottingRegisaur());
        harness.addToBattlefield(player1, new RottingRegisaur());
        RottingRegisaur discarded = new RottingRegisaur();
        harness.setHand(player1, List.of(discarded));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Rotting Regisaur")).isEqualTo(2);
    }
}
