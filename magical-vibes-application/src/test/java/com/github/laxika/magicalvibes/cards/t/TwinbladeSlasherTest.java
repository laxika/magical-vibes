package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TwinbladeSlasher.class})
class TwinbladeSlasherTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability gives +2/+2 until end of turn")
    void resolvingBoosts() {
        addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent slasher = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(slasher.getEffectivePower()).isEqualTo(3);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can only activate once each turn")
    void onlyOncePerTurn() {
        addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent slasher = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(slasher.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slasher.getEffectivePower()).isEqualTo(1);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without mana")
    void cannotActivateWithoutMana() {
        addSlasherReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activation limit applies while the first ability is still on the stack")
    void cannotActivateAgainBeforeResolution() {
        addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Different Slashers each have their own activation limit")
    void activationLimitIsPerPermanent() {
        Permanent first = addSlasherReady(player1);
        Permanent second = addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped summoning-sick Slasher can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent slasher = harness.addToBattlefieldAndReturn(player1, new TwinbladeSlasher());
        slasher.setSummoningSick(true);
        slasher.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(slasher.getEffectivePower()).isEqualTo(3);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(3);
        assertThat(slasher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activation limit resets on the opponent's next turn")
    void canActivateAgainOnOpponentsTurn() {
        Permanent slasher = addSlasherReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(slasher.getEffectivePower()).isEqualTo(3);
        assertThat(slasher.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Wither deals creature damage as permanent minus counters")
    void witherCountersPersistAfterCleanup() {
        addSlasherReady(player1);
        Permanent blocker = addSlasherReady(player2);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Twinblade Slasher");
        harness.assertLife(player2, 20);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Twinblade Slasher");
    }

    @Test
    @DisplayName("Wither damage to a player causes ordinary life loss")
    void witherDamageToPlayerLosesLife() {
        addSlasherReady(player1);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    private Permanent addSlasherReady(Player player) {
        return addCreatureReady(player, new TwinbladeSlasher());
    }
}