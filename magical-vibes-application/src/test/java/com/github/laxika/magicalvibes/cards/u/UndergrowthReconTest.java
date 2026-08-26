package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndergrowthRecon.class, Forest.class, GrizzlyBears.class})
class UndergrowthReconTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land from the graveyard tapped at upkeep")
    void returnsTargetLandTapped() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(forest));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        Permanent returnedForest = findPermanent(player1, "Forest");
        assertThat(returnedForest.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Does not target a nonland card in the graveyard")
    void onlyTargetsLands() {
        harness.addToBattlefield(player1, new UndergrowthRecon());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
