package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Subdue.class, GrizzlyBears.class, Forest.class})
class SubdueTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the target creature +0/+X where X is its mana value")
    void boostsTargetByItsManaValue() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castSubdue(target);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(2);
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
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castSubdue(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, target, false)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNonCreature() {
        Permanent forest = addCreatureReady(player2, new Forest());
        harness.setHand(player1, List.of(new Subdue()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castSubdue(Permanent target) {
        harness.setHand(player1, List.of(new Subdue()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Player defender) {
        Card bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }
}
