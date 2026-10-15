package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DoranTheSiegeTower;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarodTheBeigeFlower.class, Memnite.class, SengirVampire.class, DoranTheSiegeTower.class})
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

    @Test
    @DisplayName("Narod deals three combat damage despite having zero power")
    void narodDealsManaValueToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent narod = addCreatureReady(player1, new NarodTheBeigeFlower());
        narod.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A zero mana value attacker deals no combat damage")
    void zeroManaValueAttackerDealsNoDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new NarodTheBeigeFlower());
        Permanent memnite = addCreatureReady(player1, new Memnite());
        memnite.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Blocking creatures also deal their mana value in combat")
    void blockingCreaturesUseManaValue() {
        harness.addToBattlefield(player2, new NarodTheBeigeFlower());
        Permanent attacker = addCreatureReady(player1, new SengirVampire());
        Permanent blocker = addCreatureReady(player2, new SengirVampire());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @CardUsed({NarodTheBeigeFlower.class, DoranTheSiegeTower.class})
    @DisplayName("Competing combat damage assignment effects require a controller choice")
    void competingAssignmentEffectsOfferChoiceBeforeDamage() {
        harness.setLife(player2, 20);
        Permanent narod = addCreatureReady(player1, new NarodTheBeigeFlower());
        harness.addToBattlefield(player2, new DoranTheSiegeTower());
        narod.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }
}
