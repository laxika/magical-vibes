package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoonwingMothTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature +0/+1 until end of turn")
    void boostsTargetCreature() {
        Permanent target = addMoonwingMothAndTarget();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps Moonwing Moth when its ability is activated")
    void tapsOnActivation() {
        Permanent target = addMoonwingMothAndTarget();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(findPermanent(player1, "Moonwing Moth").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent target = addMoonwingMothAndTarget();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addMoonwingMothAndTarget() {
        harness.addToBattlefield(player1, new MoonwingMoth());
        findPermanent(player1, "Moonwing Moth").setSummoningSick(false);

        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(target);
        return target;
    }
}
