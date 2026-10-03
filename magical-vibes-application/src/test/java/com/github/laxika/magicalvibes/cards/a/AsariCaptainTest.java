package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PapercraftDecoy;
import com.github.laxika.magicalvibes.cards.s.SunbladeSamurai;
import com.github.laxika.magicalvibes.cards.u.UnstoppableOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AsariCaptain.class, SunbladeSamurai.class, UnstoppableOgre.class, PapercraftDecoy.class})
class AsariCaptainTest extends BaseCardTest {

    @Test
    void samuraiAttackingAloneGetsPowerForEachSamuraiOrWarrior() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new SunbladeSamurai());
        addCreatureReady(player1, new UnstoppableOgre());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, asariCaptain)).isEqualTo(3);
    }

    @Test
    void nonSamuraiOrWarriorDoesNotTriggerTheAbility() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new PapercraftDecoy());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(4);
    }

    @Test
    void samuraiOrWarriorAttackingWithAnotherCreatureDoesNotTriggerTheAbility() {
        Permanent asariCaptain = addCreatureReady(player1, new AsariCaptain());
        addCreatureReady(player1, new SunbladeSamurai());
        addCreatureReady(player1, new PapercraftDecoy());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, asariCaptain)).isEqualTo(4);
    }

    @Test
    void anotherSamuraiReceivesTheBoostInsteadOfCaptain() {
        Permanent captain = addCreatureReady(player1, new AsariCaptain());
        Permanent attacker = addCreatureReady(player1, new SunbladeSamurai());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
    }

    @Test
    void warriorReceivesBoostAndOnlyControlledSamuraiAndWarriorsAreCounted() {
        Permanent captain = addCreatureReady(player1, new AsariCaptain());
        Permanent warrior = addCreatureReady(player1, new UnstoppableOgre());
        addCreatureReady(player1, new PapercraftDecoy());
        addCreatureReady(player2, new SunbladeSamurai());
        addCreatureReady(player2, new UnstoppableOgre());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
    }

    @Test
    void opposingSamuraiAttackingAloneDoesNotReceiveBoost() {
        Permanent captain = addCreatureReady(player1, new AsariCaptain());
        Permanent attacker = addCreatureReady(player2, new SunbladeSamurai());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
    }

    @Test
    void countIsEvaluatedAtResolutionAndBoostDoesNotChangeAfterward() {
        Permanent captain = addCreatureReady(player1, new AsariCaptain());
        Permanent samurai = addCreatureReady(player1, new SunbladeSamurai());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(samurai);
        addCreatureReady(player1, new UnstoppableOgre());
        addCreatureReady(player1, new SunbladeSamurai());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(7);
        addCreatureReady(player1, new SunbladeSamurai());
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(7);
    }

    @Test
    void triggerStillBoostsAttackerAfterCaptainLeavesBattlefield() {
        Permanent captain = addCreatureReady(player1, new AsariCaptain());
        Permanent attacker = addCreatureReady(player1, new SunbladeSamurai());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(captain);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    void eachCaptainBoostsTheSingleAttacker() {
        Permanent attacker = addCreatureReady(player1, new AsariCaptain());
        Permanent otherCaptain = addCreatureReady(player1, new AsariCaptain());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherCaptain)).isEqualTo(4);
    }

    @Test
    void hasteAllowsNewCaptainToAttackAndBoostExpiresAtCleanup() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new AsariCaptain());
        captain.setSummoningSick(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(3);
    }
}
