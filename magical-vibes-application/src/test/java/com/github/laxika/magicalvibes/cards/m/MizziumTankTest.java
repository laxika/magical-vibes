package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MizziumTank.class, GrizzlyBears.class, Shock.class})
class MizziumTankTest extends BaseCardTest {

    @Test
    void castingNoncreatureSpellAnimatesAndBoostsTank() {
        Permanent tank = addTankReady();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(3);

        endTurn();

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void castingCreatureSpellDoesNotAnimateOrBoostTank() {
        Permanent tank = addTankReady();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void crewAnimatesTankAndTapsTheCrewCreature() {
        Permanent tank = addTankReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addTankReady() {
        Permanent tank = new Permanent(new MizziumTank());
        tank.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tank);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return tank;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
