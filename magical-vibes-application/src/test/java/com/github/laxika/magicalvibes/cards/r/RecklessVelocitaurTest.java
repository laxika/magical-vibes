package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecklessVelocitaurTest extends BaseCardTest {

    @Test
    void crewsVehicleDuringMainPhaseAndBoostsItWithTrampleUntilEndOfTurn() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower + 2);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotTriggerOutsideMainPhase() {
        addCreatureReady(player1, new RecklessVelocitaur());
        Permanent vehicle = addVehicleReady(player1);
        int basePower = gqs.getEffectivePower(gd, vehicle);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(basePower);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new DuskLegionDreadnought());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
