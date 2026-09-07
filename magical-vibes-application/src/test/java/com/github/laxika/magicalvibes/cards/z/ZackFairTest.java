package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZackFair.class, GrizzlyBears.class, LeoninScimitar.class})
class ZackFairTest extends BaseCardTest {

    @Test
    @DisplayName("Zack Fair enters with a +1/+1 counter")
    void entersWithPlusOneCounter() {
        harness.setHand(player1, List.of(new ZackFair()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent zack = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ZackFair)
                .findFirst()
                .orElseThrow();
        assertThat(zack.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Zack Fair copies every counter and grants indestructible")
    void copiesCountersAndGrantsIndestructible() {
        Permanent zack = addCreatureReady(player1, new ZackFair());
        zack.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        zack.setCounterCount(CounterType.CHARGE, 2);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zack);
    }

    @Test
    @DisplayName("Zack Fair reattaches an Equipment that was attached to it")
    void reattachesEquipment() {
        Permanent zack = addCreatureReady(player1, new ZackFair());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(zack.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Zack Fair lets its controller choose among attached Equipment")
    void choosesAmongAttachedEquipment() {
        Permanent zack = addCreatureReady(player1, new ZackFair());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        first.setAttachedTo(zack.getId());
        second.setAttachedTo(zack.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getAttachedTo()).isNull();
        assertThat(second.getAttachedTo()).isEqualTo(target.getId());
    }
}
