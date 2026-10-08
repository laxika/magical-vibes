package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({SpikeWeaver.class, SpikeHatcher.class, CityOfTraitors.class})
class SpikeWeaverTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three +1/+1 counters")
    void entersWithThreePlusOneCounters() {
        prepareMainPhase(player1);
        harness.castFromHand(player1, new SpikeWeaver(), "{2}{G}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spike Weaver")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removes a counter to put one on target creature")
    void removesCounterAndPutsCounterOnTargetCreature() {
        Permanent weaver = addReadyWeaver(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpikeHatcher());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(weaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a counter to prevent all combat damage for the turn")
    void removesCounterAndPreventsAllCombatDamage() {
        Permanent weaver = addReadyWeaver(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 1, null, null);
        harness.passBothPriorities();

        assertThat(weaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage dealt by creatures for the turn")
    void preventsCombatDamageForTheTurn() {
        Permanent weaver = addReadyWeaver(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(battlefieldIndex(player1, weaver)));
        resolveCombat();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Combat damage prevention is cleared at end of turn")
    void combatDamagePreventionIsClearedAtEndOfTurn() {
        Permanent weaver = addReadyWeaver(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.preventAllCombatDamage).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    @DisplayName("Counter ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent weaver = addReadyWeaver(player1);
        Permanent cityOfTraitors = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, weaver), 0, null, cityOfTraitors.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target itself, paying the counter before resolution")
    void canTargetItself() {
        Permanent weaver = addReadyWeaver(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 0, null, weaver.getId());

        assertThat(weaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(weaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fog resolves even after paying the last counter kills its source")
    void lastCounterFogSurvivesSourceDeath() {
        Permanent weaver = addReadyWeaver(player1);
        weaver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attacker = addReadyWeaver(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 1, null, null);

        harness.assertNotOnBattlefield(player1, "Spike Weaver");
        harness.assertInGraveyard(player1, "Spike Weaver");
        harness.passBothPriorities();
        declareAttackers(player2, List.of(battlefieldIndex(player2, attacker)));
        resolveCombat(player2);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counter transfer resolves even after the source loses its last counter")
    void lastCounterTransferSurvivesSourceDeath() {
        Permanent weaver = addReadyWeaver(player1);
        weaver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addReadyWeaver(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Spike Weaver");
        harness.assertInGraveyard(player1, "Spike Weaver");
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Targeting itself cannot save it when the last counter is spent")
    void lastCounterCannotBeReturnedToDyingSource() {
        Permanent weaver = addReadyWeaver(player1);
        weaver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 0, null, weaver.getId());

        harness.assertNotOnBattlefield(player1, "Spike Weaver");
        harness.assertInGraveyard(player1, "Spike Weaver");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spike Weaver");
        harness.assertInGraveyard(player1, "Spike Weaver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both abilities work while tapped and summoning sick")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent weaver = addReadyWeaver(player1);
        weaver.setSummoningSick(true);
        weaver.tap();
        Permanent target = addReadyWeaver(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);

        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(player1, weaver), 1, null, null);
        harness.passBothPriorities();

        assertThat(weaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    private Permanent addReadyWeaver(Player player) {
        Permanent weaver = addCreatureReady(player, new SpikeWeaver());
        weaver.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        return weaver;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
