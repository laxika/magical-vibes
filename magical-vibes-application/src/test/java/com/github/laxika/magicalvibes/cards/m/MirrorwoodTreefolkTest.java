package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorwoodTreefolkTest extends BaseCardTest {

    @Test
    void redirectsTheEntireNextDamageEvent() {
        Permanent treefolk = addReady(player1, new MirrorwoodTreefolk());
        Permanent destination = addReadyStats(player1, 4, 4);
        Permanent attacker = addReadyStats(player2, 2, 2);
        Permanent pyromancer = addReady(player2, new ProdigalPyromancer());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, destination.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(indexOf(player1, treefolk), 0)));
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(2);

        harness.activateAbility(player2, indexOf(player2, pyromancer), null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void redirectsDamageToAPlayer() {
        Permanent treefolk = addReady(player1, new MirrorwoodTreefolk());
        Permanent pyromancer = addReady(player1, new ProdigalPyromancer());
        int lifeBefore = gd.getLife(player2.getId());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, treefolk), null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, treefolk.getId());
        harness.passBothPriorities();

        assertThat(treefolk.getMarkedDamage()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReady(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
