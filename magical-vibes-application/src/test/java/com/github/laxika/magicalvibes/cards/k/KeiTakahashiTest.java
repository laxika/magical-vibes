package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeiTakahashiTest extends BaseCardTest {

    private int indexOf(Player controller, Permanent permanent) {
        return gd.playerBattlefields.get(controller.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("{T} shields the target creature for 2 damage")
    void shieldsTargetCreature() {
        harness.addToBattlefield(player1, new KeiTakahashi());
        Permanent kei = findPermanent(player1, "Kei Takahashi");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, indexOf(player1, kei), null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getDamagePreventionShield()).isEqualTo(2);
        assertThat(kei.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The shield prevents the next 2 noncombat damage")
    void shieldPreventsNextTwoNoncombatDamage() {
        harness.addToBattlefield(player1, new KeiTakahashi());
        Permanent kei = findPermanent(player1, "Kei Takahashi");
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);

        harness.activateAbility(player1, indexOf(player1, kei), null, bears.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(bears.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new KeiTakahashi());
        Permanent kei = findPermanent(player1, "Kei Takahashi");

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, kei), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new KeiTakahashi());
        Permanent kei = findPermanent(player1, "Kei Takahashi");
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, indexOf(player1, kei), null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isZero();
    }
}
