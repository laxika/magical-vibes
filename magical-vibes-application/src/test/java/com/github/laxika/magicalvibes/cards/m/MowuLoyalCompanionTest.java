package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DragonscaleBoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZameckGuildmage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MowuLoyalCompanion.class, DragonscaleBoon.class, GrizzlyBears.class, ZameckGuildmage.class})
class MowuLoyalCompanionTest extends BaseCardTest {

    @Test
    void addsOneCounterToMultipleCountersPutOnIt() {
        Permanent mowu = harness.addToBattlefieldAndReturn(player1, new MowuLoyalCompanion());
        castDragonscaleBoon(mowu);

        assertThat(mowu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotAddCounterToAnotherCreature() {
        harness.addToBattlefield(player1, new MowuLoyalCompanion());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDragonscaleBoon(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void addsOneCounterWhenItEntersWithACounter() {
        harness.addToBattlefield(player1, new ZameckGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new MowuLoyalCompanion()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mowu = findPermanent(player1, "Mowu, Loyal Companion");
        assertThat(mowu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castDragonscaleBoon(Permanent target) {
        harness.setHand(player1, List.of(new DragonscaleBoon()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
