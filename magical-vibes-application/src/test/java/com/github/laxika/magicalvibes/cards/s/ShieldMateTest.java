package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldMate.class, CityOfTraitors.class})
class ShieldMateTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives target creature +0/+4 until end of turn")
    void sacrificesAndBoostsTargetCreature() {
        Permanent shieldMate = addCreatureReady(player1, new ShieldMate());
        Permanent target = addCreatureReady(player2, new ShieldMate());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shieldMate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shieldMate.getCard());
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("The +0/+4 boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ShieldMate());
        Permanent target = addCreatureReady(player2, new ShieldMate());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new ShieldMate());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while summoning sick because the ability does not require tapping")
    void canActivateWhileSummoningSick() {
        Permanent shieldMate = harness.addToBattlefieldAndReturn(player1, new ShieldMate());
        Permanent target = addCreatureReady(player2, new ShieldMate());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shieldMate);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Pays the sacrifice cost even if the target leaves before resolution")
    void paysSacrificeCostIfTargetLeavesBeforeResolution() {
        Permanent shieldMate = addCreatureReady(player1, new ShieldMate());
        Permanent target = addCreatureReady(player2, new ShieldMate());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shieldMate);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shieldMate.getCard());
        assertThat(target.getToughnessModifier()).isZero();
    }
}
