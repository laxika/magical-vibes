package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FemerefHealer.class, FemerefScouts.class})
class FemerefHealerTest extends BaseCardTest {

    private Permanent addHealerReady() {
        return addCreatureReady(player1, new FemerefHealer());
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target creature")
    void preventsOneOnCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new FemerefScouts());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 1 damage to a target player")
    void preventsOneOnPlayer() {
        addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage to a target player is prevented")
    void preventsOnlyNextDamageToPlayer() {
        addHealerReady();
        Permanent firstAttacker = addCreatureReady(player1, new FemerefHealer());
        Permanent secondAttacker = addCreatureReady(player1, new FemerefHealer());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        firstAttacker.setAttacking(true);
        firstAttacker.setAttackTarget(player2.getId());
        secondAttacker.setAttacking(true);
        secondAttacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Prevents damage dealt to a target creature through combat")
    void preventsCombatDamageToCreature() {
        addHealerReady();
        Permanent target = addCreatureReady(player2, new FemerefScouts());
        Permanent attacker = addCreatureReady(player1, new FemerefHealer());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        target.setBlocking(true);
        target.addBlockingTarget(1);
        resolveCombat(player1);

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Activating the ability taps Femeref Healer")
    void activationTapsHealer() {
        Permanent healer = addHealerReady();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
    }

    @Test
    @CardUsed(Forest.class)
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addHealerReady();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({ChandraNalaar.class, LightningBolt.class})
    @DisplayName("Prevents the next damage to a target planeswalker")
    void preventsDamageToPlaneswalker() {
        addHealerReady();
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }
}
