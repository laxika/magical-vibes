package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberHauler.class, RuneclawBear.class, LlanowarElves.class, JaceBeleren.class, Mountain.class})
class EmberHaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Ember Hauler deals 2 damage to target player")
    void dealsDamageToPlayer() {
        addReadyEmberHauler(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ember Hauler is sacrificed as part of the cost")
    void sacrificedAsCost() {
        addReadyEmberHauler(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Ember Hauler");
        harness.assertInGraveyard(player1, "Ember Hauler");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, destroying a 2/2")
    void dealsDamageToCreatureKilling2Toughness() {
        addReadyEmberHauler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyEmberHauler(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate with summoning sickness (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new EmberHauler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target self to deal 2 damage")
    void canTargetSelf() {
        addReadyEmberHauler(player1);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyEmberHauler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Damage removes two loyalty counters from a planeswalker")
    void dealsDamageToPlaneswalker() {
        addReadyEmberHauler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Jace Beleren");
    }

    @Test
    @DisplayName("Can target a creature its controller owns")
    void canTargetOwnCreature() {
        addReadyEmberHauler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Can activate while tapped because the ability has no tap cost")
    void canActivateWhileTapped() {
        Permanent source = addReadyEmberHauler(player1);
        source.tap();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Ember Hauler");
    }

    @Test
    @DisplayName("Can target Ember Hauler itself, but the target is gone after paying the cost")
    void canTargetSourceButDoesNotResolve() {
        Permanent source = addReadyEmberHauler(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, source.getId());

        harness.assertInGraveyard(player1, "Ember Hauler");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("A land is not a legal damage target and rejection does not sacrifice the source")
    void cannotTargetLand() {
        addReadyEmberHauler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ember Hauler");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyEmberHauler(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EmberHauler());
        perm.setSummoningSick(false);
        return perm;
    }

}
