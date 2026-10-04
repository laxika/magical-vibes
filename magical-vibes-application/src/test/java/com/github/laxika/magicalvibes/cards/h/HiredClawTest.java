package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FlamecacheGecko;
import com.github.laxika.magicalvibes.cards.f.FinchFormation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiredClaw.class, FlamecacheGecko.class, FinchFormation.class})
class HiredClawTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a target opponent when one or more Lizards attack")
    void dealsDamageWhenLizardAttacks() {
        harness.addToBattlefield(player1, new HiredClaw());
        addCreatureReady(player1, new FlamecacheGecko());
        addCreatureReady(player1, new FlamecacheGecko());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1, 2));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when no Lizard attacks")
    void doesNotTriggerWithoutLizardAttacker() {
        harness.addToBattlefield(player1, new HiredClaw());
        addCreatureReady(player1, new FinchFormation());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter ability requires an opponent to have lost life this turn")
    void counterAbilityRequiresOpponentLifeLoss() {
        addCreatureReady(player1, new HiredClaw());
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    @DisplayName("Counter ability can be activated only once each turn")
    void counterAbilityOnlyOncePerTurn() {
        Permanent claw = addCreatureReady(player1, new HiredClaw());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(claw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        addActivationMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void triggersWhenHiredClawItselfAttacks() {
        addCreatureReady(player1, new HiredClaw());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        addActivationMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hired Claw").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void triggerStillDealsDamageAfterAttackingLizardLeaves() {
        harness.addToBattlefield(player1, new HiredClaw());
        Permanent attacker = addCreatureReady(player1, new FlamecacheGecko());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerForOpponentsLizardAttacking() {
        harness.addToBattlefield(player1, new HiredClaw());
        addCreatureReady(player2, new FlamecacheGecko());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllersLifeLossDoesNotEnableCounterAbility() {
        harness.addToBattlefield(player1, new HiredClaw());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent lost life this turn");
    }

    @Test
    void canActivateWhileSummoningSickOnOpponentsTurn() {
        harness.addToBattlefield(player1, new HiredClaw());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        addActivationMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hired Claw").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
