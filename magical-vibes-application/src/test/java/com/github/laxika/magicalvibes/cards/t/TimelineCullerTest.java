package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimelineCuller.class})
class TimelineCullerTest extends BaseCardTest {

    @Test
    @DisplayName("Warp from hand pays {B} and 2 life, then exiles at the next end step")
    void warpFromHandPaysManaAndLifeAndExilesAtNextEndStep() {
        TimelineCuller culler = new TimelineCuller();
        harness.setHand(player1, List.of(culler));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Timeline Culler");
        declareAttackers(List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(culler.getId())).isNotNull();
    }

    @Test
    @DisplayName("Warp from the graveyard pays {B} and 2 life, then exiles at the next end step")
    void warpFromGraveyardPaysManaAndLifeAndExilesAtNextEndStep() {
        TimelineCuller culler = new TimelineCuller();
        harness.setGraveyard(player1, List.of(culler));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.assertLife(player1, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Timeline Culler");
        declareAttackers(List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(culler.getId())).isNotNull();
    }
}
