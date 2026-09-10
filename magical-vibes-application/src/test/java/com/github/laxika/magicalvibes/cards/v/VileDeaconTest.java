package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CourtCleric;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileDeacon.class, CourtCleric.class})
class VileDeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each Cleric on the battlefield when it attacks")
    void getsPlusOneForEachClericOnBattlefield() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player1, new CourtCleric());
        addCreatureReady(player2, new CourtCleric());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(deacon.getPowerModifier()).isEqualTo(3);
        assertThat(deacon.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent deacon = addCreatureReady(player1, new VileDeacon());
        addCreatureReady(player2, new CourtCleric());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(deacon.getPowerModifier()).isEqualTo(2);
        assertThat(deacon.getToughnessModifier()).isEqualTo(2);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(deacon.getPowerModifier()).isZero();
        assertThat(deacon.getToughnessModifier()).isZero();
    }

}
