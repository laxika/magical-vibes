package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.h.HuntedWitness;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionWarboss.class, HuntedWitness.class})
class LegionWarbossTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty Goblin that must attack this combat")
    void createsGoblinThatMustAttackThisCombat() {
        Permanent warboss = addReadyWarboss(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warboss))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(warboss),
                gd.playerBattlefields.get(player1.getId()).indexOf(token)));

        assertThat(token.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on an attacking creature with lesser power")
    void mentorCountersLesserPowerAttacker() {
        Permanent warboss = addCreatureReady(player1, new LegionWarboss());
        Permanent attacker = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(warboss),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token on an opponent's turn")
    void noTokenOnOpponentsTurn() {
        harness.addToBattlefield(player1, new LegionWarboss());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Mentor excludes equal-power attackers and nonattacking creatures")
    void mentorExcludesEqualPowerAndNonattackingCreatures() {
        addCreatureReady(player1, new LegionWarboss());
        Permanent equalPower = addCreatureReady(player1, new HuntedWitness());
        equalPower.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent nonattacker = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(equalPower.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not add a counter if the target becomes equal in power")
    void mentorRechecksTargetPowerOnResolution() {
        addCreatureReady(player1, new LegionWarboss());
        Permanent attacker = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor does not add a counter if its source loses power")
    void mentorRechecksSourcePowerOnResolution() {
        Permanent warboss = addCreatureReady(player1, new LegionWarboss());
        Permanent attacker = addCreatureReady(player1, new HuntedWitness());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        warboss.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A tapped Goblin is not required to attack")
    void tappedTokenDoesNotHaveToAttack() {
        harness.addToBattlefield(player1, new LegionWarboss());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Goblin");
        token.tap();

        declareAttackers(List.of());

        assertThat(token.isAttacking()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("A Goblin is no longer required to attack after its original combat")
    void attackRequirementExpiresAfterCombat() {
        harness.addToBattlefield(player1, new LegionWarboss());
        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Goblin");
        token.tap();
        declareAttackers(List.of());
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        token.untap();

        declareAttackers(List.of());

        assertThat(token.isAttacking()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    private Permanent addReadyWarboss(Player player) {
        return addCreatureReady(player, new LegionWarboss());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

}
