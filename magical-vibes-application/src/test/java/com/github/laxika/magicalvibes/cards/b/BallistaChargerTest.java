package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallistaCharger.class, SerraAngel.class, GrizzlyBears.class})
class BallistaChargerTest extends BaseCardTest {

    @Test
    void crewingAnimatesBallistaChargerAndAttackTriggerDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent charger = addCreatureReady(player1, new BallistaCharger());
        Permanent crew = addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(charger.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(crew.isTapped()).isTrue();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void attackTriggerCanDealDamageToTargetCreature() {
        addCreatureReady(player1, new BallistaCharger());
        addCreatureReady(player1, new SerraAngel());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void crewCanCombinePowerOfSummoningSickCreatures() {
        Permanent charger = addCreatureReady(player1, new BallistaCharger());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        assertThat(gqs.isCreature(gd, charger)).isFalse();
        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, charger)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, charger)).isTrue();
    }

    @Test
    void insufficientCrewPowerCannotBePaidWithOpponentsCreatures() {
        Permanent charger = addCreatureReady(player1, new BallistaCharger());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new SerraAngel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, charger)).isFalse();
    }

    @Test
    void crewAnimationEndsAtEndOfTurn() {
        Permanent charger = addCreatureReady(player1, new BallistaCharger());
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, charger)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, charger)).isFalse();
    }
}
