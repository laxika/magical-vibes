package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEternalWanderer.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, PropheticPrism.class})
class TheEternalWandererTest extends BaseCardTest {

    @Test
    @DisplayName("+1 flickers up to one artifact or creature until its owner's next end step")
    void plusOneFlickersUntilTargetOwnersEndStep() {
        Permanent wanderer = addReadyWanderer(player1, 4);
        addCreatureReady(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        advanceToEndStep(player1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        advanceToEndStep(player2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("+1 cannot target a land")
    void plusOneCannotTargetLand() {
        addReadyWanderer(player1, 4);
        addCreatureReady(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("0 creates a double-striking 2/2 Samurai and keeps loyalty unchanged")
    void zeroCreatesSamurai() {
        Permanent wanderer = addReadyWanderer(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent samurai = findPermanent(player1, "Samurai");
        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(samurai.getEffectivePower()).isEqualTo(2);
        assertThat(samurai.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, samurai, Keyword.DOUBLE_STRIKE))
                .isTrue();
    }

    @Test
    @DisplayName("-4 leaves each player with one chosen creature and sacrifices the others")
    void minusFourKeepsOneCreaturePerPlayer() {
        Permanent wanderer = addReadyWanderer(player1, 5);
        Permanent p1Bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent p1Elves = addCreatureReady(player1, new LlanowarElves());
        Permanent p2Bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent p2Elves = addCreatureReady(player2, new LlanowarElves());
        addCreatureReady(player1, new Forest());
        addCreatureReady(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(p2Elves.getId()));

        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(p1Bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(p1Elves);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(p2Elves);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(p2Bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().getName().equals("Forest"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).filteredOn(p -> p.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Only one creature can attack The Eternal Wanderer each combat")
    void limitsAttacksAgainstWanderer() {
        Permanent wanderer = addReadyWanderer(player2, 4);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1), Map.of(
                0, wanderer.getId(),
                1, wanderer.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 1 creature can attack");
    }

    @Test
    @DisplayName("The attack limit does not limit attacks against the controller")
    void doesNotLimitAttacksAgainstController() {
        addReadyWanderer(player2, 4);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1), Map.of(
                0, player2.getId(),
                1, player2.getId()))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("+1 can be activated without a target")
    void plusOneWithoutTarget() {
        Permanent wanderer = addReadyWanderer(player1, 5);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wanderer.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("+1 can exile a noncreature artifact and return it this turn")
    void plusOneFlickersArtifact() {
        addReadyWanderer(player1, 5);
        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, prism.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Prophetic Prism");

        advanceToEndStep(player1);
        harness.assertOnBattlefield(player1, "Prophetic Prism");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("+1 returns a stolen creature to its owner even after the Wanderer leaves")
    void plusOneReturnsToOwnerWithoutSource() {
        Permanent wanderer = addReadyWanderer(player1, 5);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(wanderer);
        gd.playerGraveyards.get(player1.getId()).add(wanderer.getCard());

        advanceToEndStep(player1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        advanceToEndStep(player2);

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An exiled Samurai token does not return")
    void plusOneDoesNotReturnToken() {
        addReadyWanderer(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        Permanent samurai = findPermanent(player1, "Samurai");
        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, samurai.getId());
        harness.passBothPriorities();
        advanceToEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Samurai");
        harness.assertNotOnBattlefield(player2, "Samurai");
    }

    @Test
    @DisplayName("-4's controller chooses one of three opposing creatures to keep")
    void minusFourControllerChoosesOpponentsSurvivor() {
        addReadyWanderer(player1, 5);
        Permanent survivor = addCreatureReady(player2, new LlanowarElves());
        Permanent firstBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(survivor.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstBears.getCard(), secondBears.getCard());
    }

    @Test
    @DisplayName("-4 leaves a lone creature and resolves when the other player has none")
    void minusFourWithSingleCreature() {
        addReadyWanderer(player1, 5);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("One creature may attack the Wanderer while another attacks its controller")
    void allowsMixedAttackTargets() {
        Permanent wanderer = addReadyWanderer(player2, 5);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LlanowarElves());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1), Map.of(
                0, wanderer.getId(),
                1, player2.getId()))).doesNotThrowAnyException();
    }

    private Permanent addReadyWanderer(Player player, int loyalty) {
        Permanent permanent = addCreatureReady(player, new TheEternalWanderer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
