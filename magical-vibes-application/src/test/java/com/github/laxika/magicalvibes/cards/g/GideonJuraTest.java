package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonJura.class, RuneclawBear.class, Shock.class, Combust.class})
class GideonJuraTest extends BaseCardTest {

    @Test
    @DisplayName("+2 registers the delayed must-attack requirement pointing at Gideon himself")
    void plusTwoTauntsTowardsGideon() {
        Permanent gideon = addReadyGideon(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.tauntedNextTurn).containsEntry(player2.getId(), gideon.getId());
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("The taunted player's creatures must attack Gideon, not his controller")
    void tauntedCreaturesMustAttackGideon() {
        Permanent gideon = addReadyGideon(player1);
        gd.tauntedThisTurn.put(player2.getId(), gideon.getId());

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Attacking the player instead of Gideon is illegal while the requirement is active.
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, player1.getId())))
                .isInstanceOf(IllegalStateException.class);

        // Sitting the attack out entirely is illegal too.
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, gideon.getId()));

        harness.assertLife(player1, 20);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The requirement lapses if Gideon has left the battlefield by that turn")
    void requirementLapsesWhenGideonIsGone() {
        Permanent gideon = addReadyGideon(player1);
        gd.tauntedThisTurn.put(player2.getId(), gideon.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gideon);

        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("-2 destroys a tapped creature")
    void minusTwoDestroysTappedCreature() {
        Permanent gideon = addReadyGideon(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 can't target an untapped creature")
    void minusTwoRejectsUntappedCreature() {
        addReadyGideon(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("0 animates Gideon into a 6/6 creature")
    void zeroAnimatesGideon() {
        Permanent gideon = addReadyGideon(player1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gideon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gideon)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, gideon)).isEqualTo(6);
    }

    @Test
    @DisplayName("0 prevents all damage dealt to Gideon this turn, so he loses no loyalty")
    void zeroPreventsDamageToGideon() {
        Permanent gideon = addReadyGideon(player1);
        int loyaltyBefore = gideon.getCounterCount(CounterType.LOYALTY);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
    }

    @Test
    void plusTwoRequiresAttackingGideonDuringOpponentsNextTurn() {
        Permanent gideon = addReadyGideon(player1);
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0),
                Map.of(0, player1.getId())))
                .isInstanceOf(IllegalStateException.class);
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, gideon.getId()));
    }

    @Test
    void plusTwoCannotTargetController() {
        addReadyGideon(player1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusTwoDoesNotDestroyCreatureUntappedInResponse() {
        addReadyGideon(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.tap();
        harness.activateAbility(player1, 0, 1, null, bear.getId());
        bear.untap();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    void animationAndDamagePreventionExpireAtEndOfTurn() {
        Permanent gideon = addReadyGideon(player1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, gideon)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, gideon)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, gideon.getId());
        harness.passBothPriorities();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void unpreventableDamageMarksAnimatedGideonAndRemovesLoyalty() {
        Permanent gideon = addReadyGideon(player1);
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();
        assertThat(gideon.getMarkedDamage()).isEqualTo(5);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gideon);
    }

    private Permanent addReadyGideon(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GideonJura());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
