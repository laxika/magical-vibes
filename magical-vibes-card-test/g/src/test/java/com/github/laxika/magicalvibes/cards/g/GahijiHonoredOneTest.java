package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ElvishSkysweeper;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({GahijiHonoredOne.class, GrizzlyBears.class, ElvishSkysweeper.class, JaceBeleren.class})
class GahijiHonoredOneTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking an opponent gets +2/+0 until end of turn")
    void boostsCreatureAttackingOpponent() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A creature attacking an opponent's planeswalker gets +2/+0")
    void boostsCreatureAttackingOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = addPlaneswalker(player2);

        declareAttackerAtPermanent(player1, attacker, planeswalker);
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking the controller or the controller's planeswalker does not trigger")
    void doesNotBoostAttacksAgainstController() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent planeswalker = addPlaneswalker(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackerAtPermanent(player2, attacker, planeswalker);

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Gahiji gets its own bonus when it attacks an opponent")
    void boostsItselfWhenAttacking() {
        Permanent gahiji = addCreatureReady(player1, new GahijiHonoredOne());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gahiji.getPowerModifier()).isEqualTo(2);
        assertThat(gahiji.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Each attacking creature gets one bonus and nonattackers get none")
    void boostsEachAttackerSeparately() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent first = addCreatureReady(player1, new ElvishSkysweeper());
        Permanent second = addCreatureReady(player1, new ElvishSkysweeper());
        Permanent nonattacker = addCreatureReady(player1, new ElvishSkysweeper());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking Gahiji's controller directly does not grant a bonus")
    void doesNotBoostAttackAgainstControllerDirectly() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player2, new ElvishSkysweeper());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The bonus wears off at end of turn")
    void bonusExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new ElvishSkysweeper());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An attack trigger resolves after Gahiji leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent gahiji = harness.addToBattlefieldAndReturn(player1, new GahijiHonoredOne());
        Permanent attacker = addCreatureReady(player1, new ElvishSkysweeper());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(gahiji);
        gd.playerGraveyards.get(player1.getId()).add(gahiji.getCard());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        return planeswalker;
    }

    private void declareAttackerAtPermanent(Player attackerController, Permanent attacker, Permanent target) {
        harness.forceActivePlayer(attackerController);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(attackerController.getId()).indexOf(attacker);
        gs.declareAttackers(gd, attackerController, List.of(attackerIndex), Map.of(attackerIndex, target.getId()));
    }
}
