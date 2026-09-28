package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HumanTorchJohnnyStorm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarvelBoyNohVarr.class, GrizzlyBears.class, HumanTorchJohnnyStorm.class, AmaranthineWall.class})
class MarvelBoyNohVarrTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureEntersUnderYourControl() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsCounterWhenYouActivatePowerUpAbility() {
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addToBattlefield(player1, new HumanTorchJohnnyStorm());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOtherActivatedAbilities() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        Permanent marvelBoy = harness.addToBattlefieldAndReturn(player1, new MarvelBoyNohVarr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(marvelBoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
