package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasiveManeuvers.class, EntropicBattlecruiser.class, WorldspineWurm.class})
class InvasiveManeuversTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageWithSpacecraft() {
        harness.addToBattlefield(player1, new EntropicBattlecruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new InvasiveManeuvers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
