package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandraTheFirebrand.class, RuneclawBear.class, Shock.class, Plains.class, Divination.class})
class ChandraTheFirebrandTest extends BaseCardTest {

    @Test
    @DisplayName("+1 deals 1 damage to any target and adds loyalty")
    void plusOneDamagesPlayer() {
        Permanent chandra = addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("+1 can damage a creature")
    void plusOneDamagesCreature() {
        addReadyChandra(player1, 3);
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent bear = findPermanent(player2, "Runeclaw Bear");

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("−2 copies the next instant cast this turn, but only the first one")
    void minusTwoCopiesNextInstant() {
        Permanent chandra = addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy Shock"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("−2 delayed trigger survives a step change (it lasts the whole turn)")
    void minusTwoSurvivesManaDrain() {
        addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("−2 does not copy a creature spell")
    void minusTwoIgnoresCreatureSpell() {
        addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new RuneclawBear()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().startsWith("Copy "));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("−6 deals 6 damage to each of the chosen targets")
    void minusSixDamagesEachTarget() {
        Permanent chandra = addReadyChandra(player1, 6);
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent bear = findPermanent(player2, "Runeclaw Bear");

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bear.getId(), player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("−6 rejects a land as an \"any target\" choice")
    void minusSixRejectsLand() {
        addReadyChandra(player1, 6);
        harness.addToBattlefield(player2, new Plains());
        Permanent plains = findPermanent(player2, "Plains");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−6 rejects more than six targets")
    void minusSixRejectsSevenTargets() {
        addReadyChandra(player1, 6);
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player2, new RuneclawBear());
        }
        List<java.util.UUID> targets = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .toList();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−6 cannot be activated with only 3 loyalty")
    void minusSixNeedsSixLoyalty() {
        addReadyChandra(player1, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("The copied instant resolves, and a second instant is not copied")
    void copiedInstantResolvesOnlyOnce() {
        addReadyChandra(player1, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The copy may target a different creature while the original keeps its target")
    void copiedInstantCanChooseNewTarget() {
        addReadyChandra(player1, 3);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("A sorcery with no targets is copied, even after Chandra leaves")
    void copiesUntargetedSorceryAfterChandraDies() {
        addReadyChandra(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Chandra, the Firebrand");
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(4);
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("The delayed copy ability expires at the end of the turn")
    void delayedCopyExpiresAtEndOfTurn() {
        addReadyChandra(player1, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ultimate may be activated with zero targets")
    void ultimateAllowsZeroTargets() {
        Permanent chandra = addReadyChandra(player1, 7);

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ultimate cannot choose the same target twice")
    void ultimateRejectsRepeatedTarget() {
        addReadyChandra(player1, 7);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 2, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraTheFirebrand());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
