package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CanyonVaulterTest extends BaseCardTest {

    @Test
    void crewsVehicleDuringMainPhaseAndGrantsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addVehicleReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotTriggerOutsideMainPhase() {
        addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addVehicleReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new DuskLegionDreadnought());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
