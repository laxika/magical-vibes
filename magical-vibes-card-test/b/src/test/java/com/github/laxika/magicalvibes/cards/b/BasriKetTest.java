package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasriKet.class, AlpineWatchdog.class})
class BasriKetTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a counter on a creature and grants it indestructible")
    void plusOneCountersAndProtectsTargetCreature() {
        addReadyBasri(3);
        Permanent creature = addCreatureReady(player2, new AlpineWatchdog());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("-2 creates one tapped and attacking Soldier for each nontoken attacker")
    void minusTwoCountsOnlyNontokenAttackers() {
        addReadyBasri(3);
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());
        Card tokenCard = new AlpineWatchdog();
        tokenCard.setToken(true);
        addCreatureReady(player1, tokenCard);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(1, 2, 3));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, player2.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        });

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.isTapped()).isTrue();
            assertThat(soldier.isAttacking()).isTrue();
            assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("-6 creates an emblem that triggers at the beginning of combat")
    void ultimateEmblemCreatesAndCountersSoldier() {
        addReadyBasri(6);
        Permanent existingCreature = addCreatureReady(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 may choose no creature even when a legal target exists")
    void plusOneCanChooseNoTarget() {
        Permanent basri = addReadyBasri(3);
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(basri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 indestructible expires while the counter remains")
    void plusOneProtectionExpiresAtEndOfTurn() {
        addReadyBasri(3);
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("-2 does not trigger when only tokens attack")
    void minusTwoDoesNotCountTokenOnlyAttack() {
        addReadyBasri(3);
        Card tokenCard = new AlpineWatchdog();
        tokenCard.setToken(true);
        addCreatureReady(player1, tokenCard);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-2 survives Basri leaving and permits Soldiers to attack different defenders")
    void minusTwoSurvivesSourceAndChoosesAttackTargetsIndependently() {
        addReadyBasri(2);
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());
        Permanent defendingBasri = harness.addToBattlefieldAndReturn(player2, new BasriKet());
        defendingBasri.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.assertNotOnBattlefield(player1, "Basri Ket");
        harness.assertInGraveyard(player1, "Basri Ket");

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handlePermanentChosen(player1, defendingBasri.getId());
            harness.handlePermanentChosen(player1, player2.getId());
        });

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).extracting(Permanent::getAttackTarget)
                .containsExactlyInAnyOrder(defendingBasri.getId(), player2.getId());
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.isTapped()).isTrue();
            assertThat(soldier.isAttacking()).isTrue();
        });
    }

    @Test
    @DisplayName("-2 expires at end of turn")
    void minusTwoDoesNotTriggerOnALaterTurn() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addReadyBasri(3);
        addCreatureReady(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(1)));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The emblem ignores opponents' combat and persists after Basri leaves")
    void emblemTriggersOnlyOnControllersTurnAndPersists() {
        addReadyBasri(6);
        Permanent ownCreature = addCreatureReady(player1, new AlpineWatchdog());
        Permanent opposingCreature = addCreatureReady(player2, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);
        harness.assertNotOnBattlefield(player1, "Basri Ket");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(1);
        assertThat(soldiers.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(soldiers.getFirst().isTapped()).isFalse();
        assertThat(soldiers.getFirst().isAttacking()).isFalse();
        assertThat(soldiers.getFirst().isSummoningSick()).isTrue();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyBasri(int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new BasriKet());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
