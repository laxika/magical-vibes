package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarodTheBeigeFlower.class, Memnite.class, SengirVampire.class})
class NarodTheBeigeFlowerTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures assign combat damage equal to mana value")
    void allCreaturesUseManaValueForCombatDamage() {
        Permanent narod = harness.addToBattlefieldAndReturn(player1, new NarodTheBeigeFlower());
        Permanent zeroManaCreature = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent highManaCreature = harness.addToBattlefieldAndReturn(player2, new SengirVampire());

        assertThat(gqs.getEffectiveCombatDamage(gd, narod)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, zeroManaCreature)).isZero();
        assertThat(gqs.getEffectiveCombatDamage(gd, highManaCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The effect stops when Narod leaves the battlefield")
    void effectStopsWhenNarodLeavesBattlefield() {
        Permanent narod = harness.addToBattlefieldAndReturn(player1, new NarodTheBeigeFlower());
        Permanent zeroManaCreature = harness.addToBattlefieldAndReturn(player2, new Memnite());
        Permanent highManaCreature = harness.addToBattlefieldAndReturn(player2, new SengirVampire());

        gd.playerBattlefields.get(player1.getId()).remove(narod);

        assertThat(gqs.getEffectiveCombatDamage(gd, zeroManaCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, highManaCreature)).isEqualTo(4);
    }
}
