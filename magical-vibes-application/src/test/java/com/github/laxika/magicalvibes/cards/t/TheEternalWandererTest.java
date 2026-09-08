package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheEternalWandererTest extends BaseCardTest {

    @Test
    @DisplayName("+1 flickers up to one artifact or creature until its owner's next end step")
    void plusOneFlickersUntilTargetOwnersEndStep() {
        Permanent wanderer = addReadyWanderer(player1, 4);
        addReadyPermanent(player2, new GrizzlyBears());
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
        addReadyPermanent(player1, new Forest());
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
        Permanent p1Bears = addReadyPermanent(player1, new GrizzlyBears());
        Permanent p1Elves = addReadyPermanent(player1, new LlanowarElves());
        Permanent p2Bears = addReadyPermanent(player2, new GrizzlyBears());
        Permanent p2Elves = addReadyPermanent(player2, new LlanowarElves());
        addReadyPermanent(player1, new Forest());
        addReadyPermanent(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(p1Elves.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(p2Bears.getId()));

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
        addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player1, new LlanowarElves());

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
        addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player1, new LlanowarElves());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1), Map.of(
                0, player2.getId(),
                1, player2.getId()))).doesNotThrowAnyException();
    }

    private Permanent addReadyWanderer(Player player, int loyalty) {
        Permanent permanent = addReadyPermanent(player, new TheEternalWanderer());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
