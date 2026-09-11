package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumCustodian.class, GorillaWarrior.class, HeatRay.class, Forest.class})
class SanctumCustodianTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target creature")
    void preventsNextTwoDamageToCreature() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent target = addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstantForX(player2, 0, 3, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage only to the chosen target")
    void preventsDamageOnlyToChosenTarget() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent protectedTarget = addCreatureReady(player2, new GorillaWarrior());
        Permanent otherTarget = addCreatureReady(player2, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, protectedTarget.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstantForX(player2, 0, 1, List.of(otherTarget.getId()));
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        assertThat(otherTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target player")
    void preventsNextTwoDamageToPlayer() {
        addCreatureReady(player1, new SanctumCustodian());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GorillaWarrior());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new SanctumCustodian());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
