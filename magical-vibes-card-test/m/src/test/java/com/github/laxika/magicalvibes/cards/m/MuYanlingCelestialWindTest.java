package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.v.VialOfDragonfire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuYanlingCelestialWind.class, AirElemental.class, GreenwoodSentinel.class, VialOfDragonfire.class})
class MuYanlingCelestialWindTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives a target creature -5/-0 until Mu Yanling's next turn")
    void plusOneDebuffsUntilNextTurn() {
        addReadyMuYanling(player1, 5);
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);

        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can resolve without a target")
    void plusOneAllowsNoTarget() {
        Permanent muYanling = addReadyMuYanling(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 returns up to two target creatures to their owners' hands")
    void minusThreeReturnsUpToTwoCreatures() {
        addReadyMuYanling(player1, 5);
        Permanent first = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent second = addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("-3 can return only one creature")
    void minusThreeReturnsOneCreature() {
        addReadyMuYanling(player1, 5);
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());
        addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeRejectsNonCreature() {
        addReadyMuYanling(player1, 5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new VialOfDragonfire());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 gives your flying creatures +5/+5 until end of turn")
    void minusSevenBoostsControlledFlyingCreatures() {
        addReadyMuYanling(player1, 7);
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        Permanent groundCreature = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent opposingFlyer = addCreatureReady(player2, new AirElemental());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, groundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingFlyer)).isEqualTo(4);

        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, flyer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, flyer)).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 can resolve without targets while paying loyalty")
    void minusThreeAllowsNoTargets() {
        Permanent muYanling = addReadyMuYanling(player1, 5);
        Permanent creature = addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        assertThat(muYanling.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("-3 returns creatures controlled by either player to their owners")
    void minusThreeReturnsCreaturesFromBothBattlefields() {
        addReadyMuYanling(player1, 5);
        Permanent own = addCreatureReady(player1, new GreenwoodSentinel());
        Permanent opposing = addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(own.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opposing.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
    }

    @Test
    @DisplayName("-3 still returns the remaining legal target")
    void minusThreeResolvesWithOneTargetGone() {
        addReadyMuYanling(player1, 5);
        Permanent first = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent second = addCreatureReady(player2, new GreenwoodSentinel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, first));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(first.getCard(), second.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("+1 lasts until the controller's next turn even if Mu Yanling leaves")
    void plusOnePersistsWithoutSource() {
        Permanent muYanling = addReadyMuYanling(player1, 5);
        Permanent creature = addCreatureReady(player2, new AirElemental());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, muYanling));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 only boosts flying creatures present when it resolves")
    void minusSevenDoesNotBoostLaterCreatures() {
        addReadyMuYanling(player1, 7);
        Permanent original = addCreatureReady(player1, new AirElemental());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent later = harness.enterBattlefieldAndReturn(player1, new AirElemental());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Mu Yanling, Celestial Wind");
    }

    private Permanent addReadyMuYanling(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MuYanlingCelestialWind());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
