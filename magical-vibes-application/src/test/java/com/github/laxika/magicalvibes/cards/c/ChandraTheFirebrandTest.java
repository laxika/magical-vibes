package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

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

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy Lightning Bolt"));
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bear = findPermanent(player2, "Grizzly Bears");

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(bear.getId(), player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
            harness.addToBattlefield(player2, new GrizzlyBears());
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

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = new Permanent(new ChandraTheFirebrand());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
