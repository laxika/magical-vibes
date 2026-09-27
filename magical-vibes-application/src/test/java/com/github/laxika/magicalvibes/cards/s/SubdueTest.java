package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.r.RelicBarrier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Subdue.class, DAvenantArcher.class, AzureDrake.class, RelicBarrier.class})
class SubdueTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +0/+X where X is its mana value")
    void boostsTargetByItsManaValue() {
        Permanent target = addCreatureReady(player2, new DAvenantArcher());

        castSubdue(target);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents the target creature from dealing combat damage this turn")
    void preventsCombatDamageDealtByTarget() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2);

        castSubdue(attacker);
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage from the target creature")
    void doesNotPreventNoncombatDamage() {
        Permanent target = addCreatureReady(player1, new DAvenantArcher());
        Permanent attacker = addCreatureReady(player2, new AzureDrake());
        attacker.setAttacking(true);

        castSubdue(target);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both effects end at end of turn")
    void effectsEndAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DAvenantArcher());

        castSubdue(target);

        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RelicBarrier());
        harness.setHand(player1, List.of(new Subdue()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSubdue(Permanent target) {
        harness.setHand(player1, List.of(new Subdue()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addAttacker(Player owner, Player defender) {
        Permanent attacker = addCreatureReady(owner, new DAvenantArcher());
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }
}
