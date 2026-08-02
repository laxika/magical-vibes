package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CarnivorousMossBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts a +1/+1 counter on it, growing it permanently")
    void activationAddsCounter() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 7);

        activateAndResolve();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(6);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly, stacking counters")
    void repeatedActivationsStack() {
        Permanent beast = addBeast();
        harness.addMana(player1, ManaColor.GREEN, 14);

        activateAndResolve();
        activateAndResolve();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(7);
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addBeast() {
        Permanent perm = new Permanent(new CarnivorousMossBeast());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
