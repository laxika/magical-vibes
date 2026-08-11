package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BringLowTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToCreatureWithoutPlusOnePlusOneCounter() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageToCreatureWithPlusOnePlusOneCounter() {
        Permanent target = addCreatureReady(player2, new AvatarOfMight());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castBringLow(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent target = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castBringLow(Permanent target) {
        harness.setHand(player1, List.of(new BringLow()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
