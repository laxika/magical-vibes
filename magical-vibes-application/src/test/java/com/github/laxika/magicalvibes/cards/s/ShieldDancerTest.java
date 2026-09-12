package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FaultRiders;
import com.github.laxika.magicalvibes.cards.r.RhysticLightning;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldDancer.class, FaultRiders.class, RhysticLightning.class})
class ShieldDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next combat damage from the target attacker to that attacker")
    void redirectsNextCombatDamageToAttacker() {
        Permanent dancer = addCreatureReady(player1, new ShieldDancer());
        Permanent attacker = addCreatureReady(player2, new FaultRiders());
        TestCards.mutableCard(attacker).setToughness(5);

        declareAttackers(player2, List.of(battlefieldIndex(player2, attacker)));
        prepareDeclareBlockers(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                battlefieldIndex(player1, dancer), battlefieldIndex(player2, attacker))));

        harness.activateAbility(player1, battlefieldIndex(player1, dancer), null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat(player2);

        assertThat(dancer.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not redirect noncombat damage from the target attacker")
    void doesNotRedirectNoncombatDamage() {
        Permanent dancer = addCreatureReady(player1, new ShieldDancer());
        Permanent attacker = addCreatureReady(player2, new FaultRiders());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, dancer), null, attacker.getId());
        harness.passBothPriorities();
        resolveCombat(player2);

        harness.setHand(player2, List.of(new RhysticLightning()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, dancer.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dancer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent dancer = addCreatureReady(player1, new ShieldDancer());
        Permanent creature = addCreatureReady(player2, new FaultRiders());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, dancer), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create a shield if the target stops attacking before resolution")
    void targetMustStillBeAttackingWhenAbilityResolves() {
        Permanent dancer = addCreatureReady(player1, new ShieldDancer());
        Permanent attacker = addCreatureReady(player2, new FaultRiders());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, dancer), null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(dancer.getMarkedDamage()).isZero();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
