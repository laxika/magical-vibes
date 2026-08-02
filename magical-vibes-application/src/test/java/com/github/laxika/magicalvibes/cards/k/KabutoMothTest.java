package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KabutoMothTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature +1/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Taps Kabuto Moth when its ability is activated")
    void tapsOnActivation() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(findPermanent(player1, "Kabuto Moth").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent target = addKabutoMothAndTarget();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private Permanent addKabutoMothAndTarget() {
        harness.addToBattlefield(player1, new KabutoMoth());
        findPermanent(player1, "Kabuto Moth").setSummoningSick(false);

        Permanent target = new Permanent(new GrizzlyBears());
        target.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(target);
        return target;
    }
}
