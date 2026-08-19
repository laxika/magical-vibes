package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShieldDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next combat damage from the target attacker to that attacker")
    void redirectsNextCombatDamageToAttacker() {
        Permanent dancer = addReadyCreature(player1, new ShieldDancer());
        Permanent attacker = addReadyCreature(player2, new GrizzlyBears());
        TestCards.mutableCard(attacker).setToughness(5);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, dancer), null, attacker.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        attacker.setAttacking(true);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(battlefieldIndex(player1, dancer), 0)));
        harness.passBothPriorities();

        assertThat(dancer.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not redirect noncombat damage from the target attacker")
    void doesNotRedirectNoncombatDamage() {
        Permanent dancer = addReadyCreature(player1, new ShieldDancer());
        Permanent attacker = addReadyCreature(player2, new ProdigalPyromancer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, dancer), null, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, battlefieldIndex(player2, attacker), null, dancer.getId());
        harness.passBothPriorities();

        assertThat(dancer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent dancer = addReadyCreature(player1, new ShieldDancer());
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, dancer), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
