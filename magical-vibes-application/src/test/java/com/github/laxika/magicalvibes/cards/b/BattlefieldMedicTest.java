package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RavenousBaloth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldMedic.class, RavenousBaloth.class, Shock.class})
class BattlefieldMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage equal to the number of Clerics on the battlefield")
    void preventsDamageEqualToClericCount() {
        addCreatureReady(player1, new BattlefieldMedic());
        addCreatureReady(player2, new BattlefieldMedic());
        Permanent target = addCreatureReady(player2, new RavenousBaloth());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Counts the Battlefield Medic activating the ability")
    void countsSourceCleric() {
        Permanent medic = addCreatureReady(player1, new BattlefieldMedic());
        Permanent target = addCreatureReady(player2, new RavenousBaloth());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(medic.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts Clerics on the battlefield when the ability resolves")
    void countsClericsAtResolution() {
        Permanent target = addCreatureReady(player2, new RavenousBaloth());
        addCreatureReady(player1, new BattlefieldMedic());

        harness.activateAbility(player1, 0, null, target.getId());
        addCreatureReady(player2, new BattlefieldMedic());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage to the targeted creature")
    void preventsCombatDamageToTargetCreature() {
        addCreatureReady(player1, new BattlefieldMedic());
        Permanent attacker = addCreatureReady(player1, new RavenousBaloth());
        Permanent target = addCreatureReady(player2, new RavenousBaloth());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        target.setBlocking(true);
        target.addBlockingTarget(1);
        resolveCombat(player1);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new BattlefieldMedic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
