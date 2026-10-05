package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalazzoArchers.class, AirElemental.class, GrizzlyBears.class})
class PalazzoArchersTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature attacking you triggers damage equal to Palazzo Archers' power")
    void flyingCreatureAttackingPlayerTakesSourcePowerDamage() {
        Permanent archers = addCreatureReady(player1, new PalazzoArchers());
        Permanent attacker = addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0));
        archers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveTrigger();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed(JaceBeleren.class)
    @DisplayName("A flying creature attacking your planeswalker also triggers")
    void flyingCreatureAttackingPlaneswalkerTakesSourcePowerDamage() {
        addCreatureReady(player1, new PalazzoArchers());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        Permanent attacker = addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveTrigger();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-flying creature does not trigger Palazzo Archers")
    void nonFlyingCreatureDoesNotTrigger() {
        addCreatureReady(player1, new PalazzoArchers());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private void resolveTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    @Test
    void eachFlyingAttackerReceivesDamage() {
        addCreatureReady(player1, new PalazzoArchers());
        Permanent first = addCreatureReady(player2, new AirElemental());
        Permanent second = addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveTrigger();
        resolveTrigger();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void removedSourceUsesPowerImmediatelyBeforeLeaving() {
        Permanent archers = addCreatureReady(player1, new PalazzoArchers());
        Permanent attacker = addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0));
        archers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, archers));
        resolveTrigger();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void losingAllAbilitiesPreventsAttackTrigger() {
        Permanent archers = addCreatureReady(player1, new PalazzoArchers());
        archers.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(InvasionOfZendikar.class)
    void attackingBattleDoesNotTriggerItsControllersArchers() {
        addCreatureReady(player2, new PalazzoArchers());
        Permanent attacker = addCreatureReady(player2, new AirElemental());
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player1.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(1), Map.of(1, battle.getId())));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }
}
