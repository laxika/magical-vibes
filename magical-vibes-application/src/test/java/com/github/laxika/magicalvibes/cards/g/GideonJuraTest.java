package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear);

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

        Permanent bear = new Permanent(new GrizzlyBears());
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bear);

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
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.tap();
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 can't target an untapped creature")
    void minusTwoRejectsUntappedCreature() {
        addReadyGideon(player1);
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

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

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, gideon.getId());
        harness.passBothPriorities();

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(loyaltyBefore);
    }

    private Permanent addReadyGideon(Player player) {
        Permanent perm = new Permanent(new GideonJura());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
