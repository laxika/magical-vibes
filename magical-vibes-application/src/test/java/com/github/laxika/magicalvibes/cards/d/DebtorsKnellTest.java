package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.h.HatchingPlans;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DebtorsKnell.class, Gristleback.class, HatchingPlans.class})
class DebtorsKnellTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from any graveyard under its controller's control")
    void returnsCreatureFromAnyGraveyardUnderItsControllerControl() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card creature = new Gristleback();
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gristleback");
        harness.assertNotOnBattlefield(player2, "Gristleback");
        harness.assertNotInGraveyard(player2, "Gristleback");
    }

    @Test
    @DisplayName("Returns a target creature card from its controller's graveyard")
    void returnsCreatureFromItsControllersGraveyard() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card gristleback = new Gristleback();
        harness.setGraveyard(player1, List.of(gristleback));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(gristleback.getId());

        harness.handleMultipleCardsChosen(player1, List.of(gristleback.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gristleback");
        harness.assertNotInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("Only creature cards are legal upkeep targets")
    void onlyCreatureCardsAreLegalTargets() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        harness.setGraveyard(player2, List.of(new HatchingPlans()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Hatching Plans");
    }
}
