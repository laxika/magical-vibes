package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FangOfShigeki;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighSpeedHoverbike.class, FangOfShigeki.class})
class HighSpeedHoverbikeTest extends BaseCardTest {

    @Test
    void entersAndTapsUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FangOfShigeki());

        harness.castFromHand(player1, new HighSpeedHoverbike(), "{2}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canEnterWithoutChoosingATarget() {
        harness.castFromHand(player1, new HighSpeedHoverbike(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void crewAnimatesHoverbikeAndTapsCrew() {
        Permanent hoverbike = addHoverbikeReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new FangOfShigeki());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hoverbike)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void canDeclineTargetEvenWhenACreatureIsAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FangOfShigeki());

        harness.castFromHand(player1, new HighSpeedHoverbike(), "{2}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "High-Speed Hoverbike");
    }

    @Test
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FangOfShigeki());

        harness.castFromHand(player1, new HighSpeedHoverbike(), "{2}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "High-Speed Hoverbike");
    }

    @Test
    void crewIsPaidBeforeResolutionAndAnimationEndsAtCleanup() {
        Permanent hoverbike = addHoverbikeReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new FangOfShigeki());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, hoverbike)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hoverbike)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, hoverbike)).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, hoverbike)).isFalse();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        Permanent hoverbike = addHoverbikeReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new FangOfShigeki());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, hoverbike)).isFalse();
    }

    @Test
    void crewedHoverbikeCannotBeBlockedByGroundCreature() {
        Permanent hoverbike = addHoverbikeReady();
        harness.addToBattlefield(player1, new FangOfShigeki());
        Permanent blocker = addCreatureReady(player2, new FangOfShigeki());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bls.canBlockAttacker(gd, blocker, hoverbike,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    private Permanent addHoverbikeReady() {
        return addCreatureReady(player1, new HighSpeedHoverbike());
    }
}
