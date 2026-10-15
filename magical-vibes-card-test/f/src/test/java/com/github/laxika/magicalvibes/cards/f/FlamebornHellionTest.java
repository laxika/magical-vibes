package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlamebornHellion.class, Arrest.class})
class FlamebornHellionTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyAfterResolving() {
        harness.castFromHand(player1, new FlamebornHellion(), "{5}{R}");
        harness.passBothPriorities();
        Permanent hellion = findPermanent(player1, "Flameborn Hellion");

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(hellion.isAttacking()).isTrue();
    }

    @Test
    void mustAttackEvenOnTheTurnItEnters() {
        harness.addToBattlefield(player1, new FlamebornHellion());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void everyAbleHellionMustAttack() {
        harness.addToBattlefield(player1, new FlamebornHellion());
        harness.addToBattlefield(player1, new FlamebornHellion());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void doesNotHaveToAttackWhenTapped() {
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, new FlamebornHellion());
        hellion.tap();

        declareAttackers(List.of());

        assertThat(hellion.isAttacking()).isFalse();
    }

    @Test
    void attackRestrictionOverridesAttackRequirement() {
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, new FlamebornHellion());
        harness.setHand(player1, List.of(new Arrest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, hellion.getId());
        harness.passBothPriorities();

        declareAttackers(List.of());

        assertThat(hellion.isAttacking()).isFalse();
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
