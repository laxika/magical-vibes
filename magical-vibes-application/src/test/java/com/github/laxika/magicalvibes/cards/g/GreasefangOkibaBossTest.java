package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreasefangOkibaBoss.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class GreasefangOkibaBossTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Vehicle from the graveyard with haste and returns it to hand at the next end step")
    void returnsVehicleWithHasteAndReturnsItToHandAtNextEndStep() {
        Card vehicle = new DuskLegionDreadnought();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();

        harness.handleMultipleCardsChosen(player1, List.of(vehicle.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(vehicle.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dusk Legion Dreadnought");
        harness.assertInHand(player1, "Dusk Legion Dreadnought");
    }

    @Test
    @DisplayName("Cannot target a non-Vehicle card in the graveyard")
    void cannotTargetNonVehicleCard() {
        Card nonVehicle = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonVehicle));
        harness.addToBattlefield(player1, new GreasefangOkibaBoss());

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
