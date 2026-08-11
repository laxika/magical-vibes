package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StormbreathDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts three +1/+1 counters on Stormbreath Dragon and deals damage based on the opponent's hand")
    void monstrosityAddsCountersAndDealsHandSizeDamage() {
        Permanent dragon = addReadyDragon();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(dragon.isMonstrous()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Monstrosity deals no damage to an opponent with an empty hand")
    void monstrosityDealsNoDamageForEmptyHand() {
        addReadyDragon();
        harness.setHand(player2, List.of());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Stormbreath Dragon has protection from white")
    void hasProtectionFromWhite() {
        Permanent dragon = addReadyDragon();

        assertThat(gqs.hasProtectionFrom(gd, dragon, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, dragon, CardColor.RED)).isFalse();
    }

    private Permanent addReadyDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new StormbreathDragon());
        dragon.setSummoningSick(false);
        return dragon;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
