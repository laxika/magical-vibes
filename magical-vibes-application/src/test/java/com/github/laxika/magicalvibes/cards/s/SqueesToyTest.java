package com.github.laxika.magicalvibes.cards.s;

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

class SqueesToyTest extends BaseCardTest {

    private int indexOf(Player controller, Permanent permanent) {
        return gd.playerBattlefields.get(controller.getId()).indexOf(permanent);
    }

    private Permanent addToy() {
        harness.addToBattlefield(player1, new SqueesToy());
        return findPermanent(player1, "Squee's Toy");
    }

    @Test
    @DisplayName("{T} shields the target creature for 1")
    void shieldsTargetCreature() {
        Permanent toy = addToy();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, indexOf(player1, toy), null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getDamagePreventionShield()).isEqualTo(1);
        assertThat(toy.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The shield prevents the next 1 noncombat damage")
    void shieldPreventsNoncombatDamage() {
        Permanent toy = addToy();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);

        harness.activateAbility(player1, indexOf(player1, toy), null, bears.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(0);
        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent toy = addToy();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, toy), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevention shield clears at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent toy = addToy();
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");

        harness.activateAbility(player1, indexOf(player1, toy), null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getDamagePreventionShield()).isEqualTo(0);
    }
}
