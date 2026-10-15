package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AerialBoost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarsusDepthguard.class, GrizzlyBears.class, AerialBoost.class})
class KarsusDepthguardTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack while its power is less than 5")
    void cannotAttackBelowPowerFive() {
        Permanent depthguard = addCreatureReady(player1, new KarsusDepthguard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(depthguard.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack when its power is 5 or greater")
    void canAttackAtPowerFive() {
        Permanent depthguard = addCreatureReady(player1, new KarsusDepthguard());
        depthguard.setPowerModifier(1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(depthguard.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A resolved power boost allows it to attack")
    void canAttackWithResolvedPowerBoost() {
        Permanent depthguard = addCreatureReady(player1, new KarsusDepthguard());
        harness.addToBattlefield(player2, new KarsusDepthguard());
        harness.setHand(player1, List.of(new AerialBoost()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, depthguard.getId());
        declareAttackers(List.of(0));

        assertThat(depthguard.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Losing power before attackers are declared removes the permission")
    void cannotAttackAfterPowerDropsBelowFive() {
        Permanent depthguard = addCreatureReady(player1, new KarsusDepthguard());
        depthguard.setPowerModifier(1);
        assertThat(als.canAttack(gd, depthguard, player1.getId())).isTrue();

        depthguard.setPowerModifier(0);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(depthguard.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("High power does not bypass summoning sickness")
    void cannotAttackWithSummoningSicknessAtPowerFive() {
        Permanent depthguard = harness.addToBattlefieldAndReturn(player1, new KarsusDepthguard());
        depthguard.setPowerModifier(1);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(depthguard.isAttacking()).isFalse();
    }
}
