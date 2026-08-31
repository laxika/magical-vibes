package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimicRagworm.class})
class SimicRagwormTest extends BaseCardTest {

    @Test
    void payingBlueManaUntapsSimicRagworm() {
        Permanent ragworm = addRagwormReady();
        ragworm.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ragworm.isTapped()).isFalse();
    }

    @Test
    void activatingAbilityDoesNotTapSimicRagworm() {
        Permanent ragworm = addRagwormReady();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(ragworm.isTapped()).isFalse();
    }

    private Permanent addRagwormReady() {
        Permanent ragworm = new Permanent(new SimicRagworm());
        ragworm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ragworm);
        return ragworm;
    }
}
