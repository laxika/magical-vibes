package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DistrictMascot;
import com.github.laxika.magicalvibes.cards.d.DuneDrifter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanyonVaulter.class, DuneDrifter.class, DistrictMascot.class})
class CanyonVaulterTest extends BaseCardTest {

    @BeforeEach
    void keepPriorityAtRelevantSteps() {
        for (var player : java.util.List.of(player1, player2)) {
            gd.playerAutoStopSteps.put(player.getId(), java.util.EnumSet.of(
                    TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN,
                    TurnStep.END_STEP, TurnStep.UPKEEP));
        }
    }

    @Test
    void crewsVehicleDuringMainPhaseAndGrantsFlyingUntilEndOfTurn() {
        addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addCreatureReady(player1, new DuneDrifter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotTriggerOutsideMainPhase() {
        addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addCreatureReady(player1, new DuneDrifter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    void saddlesMountAndGrantsFlyingToMountOnly() {
        Permanent vaulter = addCreatureReady(player1, new CanyonVaulter());
        Permanent mount = harness.enterBattlefieldAndReturn(player1, new DistrictMascot());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(vaulter.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, mount, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, vaulter, Keyword.FLYING)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(mount.isSaddled()).isTrue();
        assertThat(gqs.hasKeyword(gd, mount, Keyword.FLYING)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, mount, Keyword.FLYING)).isFalse();
    }

    @Test
    void crewsDuringPostcombatMainPhase() {
        Permanent vaulter = addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addCreatureReady(player1, new DuneDrifter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, vaulter, Keyword.FLYING)).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsMainPhase() {
        addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addCreatureReady(player1, new DuneDrifter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    void triggerStillGrantsFlyingAfterVaulterLeavesBattlefield() {
        Permanent vaulter = addCreatureReady(player1, new CanyonVaulter());
        Permanent vehicle = addCreatureReady(player1, new DuneDrifter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(vaulter);
        gd.playerGraveyards.get(player1.getId()).add(vaulter.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }
}
