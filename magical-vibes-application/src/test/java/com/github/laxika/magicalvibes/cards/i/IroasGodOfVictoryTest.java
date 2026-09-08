package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BorosMastiff;
import com.github.laxika.magicalvibes.cards.b.BorosReckoner;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MountainBandit;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IroasGodOfVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Iroas is not a creature below seven combined red and white devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent iroas = addIroas();
        addDevotionPermanents(1);

        assertThat(gqs.isCreature(gd, iroas)).isFalse();
        assertThat(gqs.isEnchantment(gd, iroas)).isTrue();
    }

    @Test
    @DisplayName("Iroas becomes a creature at seven combined red and white devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent iroas = addIroas();
        addDevotionAtThreshold();

        assertThat(gqs.isCreature(gd, iroas)).isTrue();
    }

    @Test
    @DisplayName("Creatures you control have menace")
    void grantsMenaceToYourCreatures() {
        addIroas();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Combat damage to attacking creatures you control is prevented")
    void preventsCombatDamageToYourAttacker() {
        addIroas();
        Permanent attacker = addAttacker(player1.getId());
        addBlocker(player2.getId(), gd.playerBattlefields.get(player1.getId()).indexOf(attacker));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Noncombat damage to an attacking creature you control is not prevented")
    void doesNotPreventNoncombatDamage() {
        addIroas();
        Permanent attacker = addAttacker(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addIroas() {
        return harness.addToBattlefieldAndReturn(player1, new IroasGodOfVictory());
    }

    private void addDevotionPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new BorosReckoner());
        }
    }

    private void addDevotionAtThreshold() {
        harness.addToBattlefield(player1, new BorosReckoner());
        harness.addToBattlefield(player1, new BorosMastiff());
        harness.addToBattlefield(player1, new MountainBandit());
    }

    private Permanent addAttacker(UUID controllerId) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private void addBlocker(UUID controllerId, int blockingTarget) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
    }
}
