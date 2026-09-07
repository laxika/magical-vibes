package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WalkingDead.class)
class WalkingDeadTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityTargetsWalkingDeadAndConsumesMana() {
        Permanent walkingDead = addWalkingDeadReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(walkingDead.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(walkingDead.isTapped()).isFalse();
    }

    @Test
    void resolvingRegenerationAbilityGrantsShield() {
        Permanent walkingDead = addWalkingDeadReady();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(walkingDead.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addWalkingDeadReady() {
        Permanent permanent = new Permanent(new WalkingDead());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
