package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaveningWarg.class, GrizzlyBears.class, RenegadeFreighter.class})
class RaveningWargTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while controlling a creature with power 4 or greater gains 2 life")
    void ferociousGainsLife() {
        Permanent warg = addCreatureReady(player1, new RaveningWarg());
        Permanent largeCreature = addCreatureReady(player1, new GrizzlyBears());
        largeCreature.setPowerModifier(2);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warg)));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Attacking without a creature with power 4 or greater does not gain life")
    void ferociousDoesNotTriggerWithoutLargeCreature() {
        Permanent warg = addCreatureReady(player1, new RaveningWarg());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warg)));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void wargCanSatisfyItsOwnPowerCondition() {
        Permanent warg = addCreatureReady(player1, new RaveningWarg());
        warg.setPowerModifier(2);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void opponentsLargeCreatureDoesNotEnableLifeGain() {
        addCreatureReady(player1, new RaveningWarg());
        Permanent opposingWarg = addCreatureReady(player2, new RaveningWarg());
        opposingWarg.setPowerModifier(2);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void losingQualifyingPowerAfterAttackingDoesNotPreventLifeGain() {
        Permanent warg = addCreatureReady(player1, new RaveningWarg());
        warg.setPowerModifier(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        warg.setPowerModifier(0);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void gainingQualifyingPowerAfterAttackingDoesNotCreateTrigger() {
        Permanent warg = addCreatureReady(player1, new RaveningWarg());
        warg.setPowerModifier(1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).isEmpty();
        warg.setPowerModifier(2);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void uncrewedVehicleDoesNotSatisfyCreaturePowerCondition() {
        addCreatureReady(player1, new RaveningWarg());
        harness.addToBattlefield(player1, new RenegadeFreighter());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
