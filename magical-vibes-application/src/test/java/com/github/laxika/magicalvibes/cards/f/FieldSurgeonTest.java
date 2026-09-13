package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieldSurgeon.class, GrizzlyBears.class, ProdigalPyromancer.class, Forest.class})
class FieldSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a creature gives the target creature a one-damage prevention shield")
    void preventsNextDamageToTargetCreature() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("May tap itself to pay the cost and target a creature it controls")
    void canTapItselfToPayCostAndTargetOwnCreature() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();

        harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId());
        harness.passBothPriorities();

        assertThat(surgeon.isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without an untapped creature to tap")
    void requiresUntappedCreatureToTap() {
        Permanent surgeon = addSurgeonWithCostCreature();
        findPermanent(player1, "Grizzly Bears").tap();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, surgeon), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent surgeon = addSurgeonWithCostCreature();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, surgeon), null, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addSurgeonWithCostCreature() {
        Permanent surgeon = addCreatureReady(player1, new FieldSurgeon());
        surgeon.tap();
        addCreatureReady(player1, new GrizzlyBears());
        return surgeon;
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
