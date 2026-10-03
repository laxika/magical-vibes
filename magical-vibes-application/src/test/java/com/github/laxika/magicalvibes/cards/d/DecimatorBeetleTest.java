package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecimatorBeetle.class, AirElemental.class, GrizzlyBears.class})
class DecimatorBeetleTest extends BaseCardTest {


    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control")
    void etbPutsCounterOnControlledCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new DecimatorBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0, elemental.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature you don't control")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new DecimatorBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }


    @Test
    @DisplayName("Attacking queues the two-step counter-move target selection")
    void attackQueuesCounterMoveTargetSelection() {
        addCreatureReady(player1, new DecimatorBeetle());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackCounterMoveFirstTarget.class);
    }

    @Test
    @DisplayName("Attack moves a -1/-1 counter from your creature onto a defending creature")
    void attackMovesCounterFromControlledToDefending() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());   // stage 1: remove from own creature
        harness.handlePermanentChosen(player1, defender.getId());      // stage 2: put on defender
        harness.passBothPriorities();                                  // resolve triggered ability

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack puts a counter on the defender even with no counter to remove")
    void attackPutsCounterEvenWhenNothingToRemove() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears()); // no -1/-1 counter
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, defender.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack still puts a counter when the remove target leaves before resolution")
    void attackPutsCounterWhenRemoveTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack still removes a counter when the defender target leaves before resolution")
    void attackRemovesCounterWhenDefenderTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player2.getId()).remove(defender);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(defender);
    }

    @Test
    @DisplayName("Attack can decline the optional second target")
    void attackDeclinesSecondTarget() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, player1.getId());       // decline (choose self)
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Second target must be a creature the defending player controls")
    void attackSecondTargetCannotBeOwnCreature() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherOwnCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        harness.handlePermanentChosen(player1, ownCreature.getId());   // stage 1 ok
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, otherOwnCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack removes a counter when the defending player controls no creatures")
    void attackRemovesCounterWithNoDefendingCreatures() {
        Permanent beetle = addCreatureReady(player1, new DecimatorBeetle());
        beetle.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, beetle.getId());
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack does not remove a counter from a creature now controlled by the opponent")
    void attackDoesNotRemoveCounterAfterFirstTargetChangesController() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new DecimatorBeetle());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new DecimatorBeetle());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player2.getId()).add(ownCreature);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attack does not put a counter on a creature no longer controlled by the defender")
    void attackDoesNotPutCounterAfterSecondTargetChangesController() {
        addCreatureReady(player1, new DecimatorBeetle());
        Permanent ownCreature = addCreatureReady(player1, new DecimatorBeetle());
        ownCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent defender = addCreatureReady(player2, new DecimatorBeetle());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, defender.getId());
        gd.playerBattlefields.get(player2.getId()).remove(defender);
        gd.playerBattlefields.get(player1.getId()).add(defender);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(defender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
