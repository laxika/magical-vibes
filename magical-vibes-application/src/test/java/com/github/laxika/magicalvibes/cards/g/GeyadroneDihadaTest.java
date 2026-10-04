package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeyadroneDihada.class, Forest.class, GrizzlyBears.class, ProdigalPyromancer.class})
class GeyadroneDihadaTest extends BaseCardTest {

    @Test
    @DisplayName("+1 drains each opponent and puts a corruption counter on up to one other creature")
    void plusOneDrainsAndCorruptsTarget() {
        Permanent dihada = addReadyDihada(player1, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
        assertThat(bear.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 cannot target Geyadrone herself")
    void plusOneCannotTargetSource() {
        Permanent dihada = addReadyDihada(player1, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, dihada.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 steals, untaps, corrupts, and gives haste to a target creature")
    void minusThreeStealsUntapsCorruptsAndGivesHaste() {
        Permanent dihada = addReadyDihada(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.tap();

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 gains control of every permanent with a corruption counter")
    void minusSevenGainsControlOfCorruptedPermanents() {
        addReadyDihada(player1, 8);
        Permanent corruptedBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent corruptedForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ordinaryForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        corruptedBear.setCounterCount(CounterType.CORRUPTION, 1);
        corruptedForest.setCounterCount(CounterType.CORRUPTION, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(corruptedBear, corruptedForest)
                .doesNotContain(ordinaryForest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ordinaryForest);
    }

    @Test
    @DisplayName("Geyadrone has protection from permanents with corruption counters")
    void protectionFromCorruptedPermanentSources() {
        Permanent dihada = addReadyDihada(player1, 5);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        pyromancer.setCounterCount(CounterType.CORRUPTION, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, dihada.getId()))
                .isInstanceOf(IllegalStateException.class);

        pyromancer.setCounterCount(CounterType.CORRUPTION, 0);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, dihada.getId());
        harness.passBothPriorities();

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOneCanResolveWithoutChoosingATarget() {
        Permanent dihada = addReadyDihada(player1, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneDoesNotDrainWhenItsOnlyTargetLeaves() {
        Permanent dihada = addReadyDihada(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneCanCorruptAnotherPlaneswalker() {
        addReadyDihada(player1, 4);
        Permanent otherDihada = addReadyDihada(player2, 4);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 0, null, otherDihada.getId());
        harness.passBothPriorities();

        assertThat(otherDihada.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
    }

    @Test
    void plusOneDoesNotDrainIfItsSourceBecomesProtectedAgainst() {
        Permanent source = addReadyDihada(player1, 4);
        Permanent target = addReadyDihada(player2, 4);
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        source.setCounterCount(CounterType.CORRUPTION, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        assertThat(target.getCounterCount(CounterType.CORRUPTION)).isZero();
    }

    @Test
    void loyaltyAbilitiesCannotTargetALand() {
        addReadyDihada(player1, 5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeControlAndHasteExpireButCorruptionRemains() {
        addReadyDihada(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
        assertThat(bear.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
    }

    @Test
    void minusThreeCanTargetSelfAndStillResolvesAfterAddingCorruption() {
        Permanent dihada = addReadyDihada(player1, 5);
        dihada.tap();

        harness.activateAbility(player1, 0, 1, null, dihada.getId());
        harness.passBothPriorities();

        assertThat(dihada.isTapped()).isFalse();
        assertThat(dihada.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dihada, Keyword.HASTE)).isTrue();
    }

    @Test
    void minusThreeDoesNotResolveIfSourceGainsCorruptionBeforeResolution() {
        Permanent dihada = addReadyDihada(player1, 5);
        dihada.tap();

        harness.activateAbility(player1, 0, 1, null, dihada.getId());
        dihada.setCounterCount(CounterType.CORRUPTION, 1);
        harness.passBothPriorities();

        assertThat(dihada.isTapped()).isTrue();
        assertThat(dihada.getCounterCount(CounterType.CORRUPTION)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dihada, Keyword.HASTE)).isFalse();
    }

    @Test
    void ultimateControlPersistsAfterSourceLeavesAndCountersAreRemoved() {
        addReadyDihada(player1, 7);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setCounterCount(CounterType.CORRUPTION, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        bear.setCounterCount(CounterType.CORRUPTION, 0);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(GeyadroneDihada.class::isInstance);
    }

    @Test
    void corruptedCreatureCanAttackDihadaButDealsNoDamage() {
        Permanent dihada = addReadyDihada(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.setSummoningSick(false);
        bear.setCounterCount(CounterType.CORRUPTION, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, dihada.getId()));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(dihada.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    private Permanent addReadyDihada(Player player, int loyalty) {
        Permanent dihada = harness.addToBattlefieldAndReturn(player, new GeyadroneDihada());
        dihada.setCounterCount(CounterType.LOYALTY, loyalty);
        dihada.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dihada;
    }
}
