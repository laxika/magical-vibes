package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrneryDilophosaur.class, ColossalDreadmaw.class, SkysovereignConsulFlagship.class})
class OrneryDilophosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a boost without a creature with power 4 or greater")
    void doesNotBoostWithoutLargeCreature() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +2/+2 when it attacks while its controller has a creature with power 4 or greater")
    void boostsWithLargeCreature() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        addCreatureReady(player1, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isEqualTo(0);
    }

    @Test
    void doesNotBoostForOpponentsLargeCreature() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isZero();
    }

    @Test
    void doesNotTriggerForUncrewedVehicle() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isZero();
    }

    @Test
    void doesNotTriggerBelowFourPowerEvenIfPowerIncreasesLater() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        PermanentGrants.setPowerBoost(gd, dilophosaur, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        PermanentGrants.setPowerBoost(gd, dilophosaur, 2);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isZero();
    }

    @Test
    void canQualifyItselfAtExactlyFourPower() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        PermanentGrants.setPowerBoost(gd, dilophosaur, 2);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isEqualTo(2);
    }

    @Test
    void doesNotBoostIfQualifyingCreatureLosesPowerBeforeResolution() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        Permanent largeCreature = addCreatureReady(player1, new ColossalDreadmaw());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        PermanentGrants.setPowerBoost(gd, largeCreature, -3);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isZero();
    }

    @Test
    void differentCreatureCanSatisfyConditionAtResolution() {
        Permanent dilophosaur = addCreatureReady(player1, new OrneryDilophosaur());
        Permanent largeCreature = addCreatureReady(player1, new ColossalDreadmaw());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        PermanentGrants.setPowerBoost(gd, largeCreature, -3);
        addCreatureReady(player1, new ColossalDreadmaw());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, dilophosaur)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, dilophosaur)).isEqualTo(2);
    }
}
