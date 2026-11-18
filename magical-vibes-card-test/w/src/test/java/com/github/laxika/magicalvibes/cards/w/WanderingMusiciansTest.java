package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WanderingMusicians.class})
class WanderingMusiciansTest extends BaseCardTest {

    @Test
    void attackingBoostsAllCreaturesYouControl() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());
        Permanent opposingCreature = addCreatureReady(player2, new WanderingMusicians());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, musicians)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, opposingCreature)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, musicians)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCreature)).isZero();
    }

    @Test
    void attackBonusExpiresAtEndOfTurn() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, musicians)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, musicians)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isZero();
    }

    @Test
    void eachAttackingMusicianAddsItsOwnBonus() {
        Permanent first = addCreatureReady(player1, new WanderingMusicians());
        Permanent second = addCreatureReady(player1, new WanderingMusicians());
        Permanent nonattacker = addCreatureReady(player1, new WanderingMusicians());
        Permanent opponent = addCreatureReady(player2, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
    }

    @Test
    void creaturesEnteringBeforeResolutionReceiveBonusButLaterCreaturesDoNot() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new WanderingMusicians());

        assertThat(PermanentGrants.powerBoostOf(gd, musicians)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, beforeResolution)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, afterResolution)).isZero();
    }

    @Test
    void attackTriggerResolvesAfterMusiciansLeaveBattlefield() {
        Permanent musicians = addCreatureReady(player1, new WanderingMusicians());
        Permanent otherCreature = addCreatureReady(player1, new WanderingMusicians());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(musicians);
        harness.setGraveyard(player1, List.of(musicians.getCard()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(PermanentGrants.powerBoostOf(gd, otherCreature)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, otherCreature)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
