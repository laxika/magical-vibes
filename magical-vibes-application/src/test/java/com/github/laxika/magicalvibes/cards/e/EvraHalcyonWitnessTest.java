package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.RhoxFaithmender;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvraHalcyonWitness.class, RhoxFaithmender.class, EverybodyLives.class})
class EvraHalcyonWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Exchange sets life to power and power to old life total")
    void exchangeLifeAndPower() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        // Default starting life is 20, Evra's power is 4
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 4 (old power), power becomes 20 (old life)
        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(20);
    }

    @Test
    @DisplayName("Exchange when life is lower than power raises life")
    void exchangeWhenLifeLowerThanPower() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 4, power becomes 2
        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exchange with equal life and unmodified power leaves both values unchanged")
    void exchangeWhenLifeEqualsPower() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple exchanges: second exchange uses updated power")
    void multipleExchanges() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        // Life=20, Power=4

        // First exchange: life->4, power->20
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(20);

        // Second exchange: life->20, power->4
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power override persists across turns")
    void powerPersistsAcrossTurns() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Power is now 20
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(20);

        // Simulate turn reset (modifiers cleared, static recomputed)
        evra.resetModifiers();

        // Permanent base power override should survive
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(20);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of exchanged power")
    void countersApplyOnTopOfExchangedPower() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        evra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        // Effective power = 4 + 2 = 6

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 6 (effective power including counters)
        // New base power = 20 (old life), + 2 counters = 22
        assertThat(gd.getLife(player1.getId())).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(22);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability does not tap Evra")
    void abilityDoesNotTap() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(evra.isTapped()).isFalse();
    }

    @Test
    void equalLifeAndModifiedPowerStillSetsBasePower() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        evra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 6);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(8);
    }

    @Test
    void exchangeAppliesLifeGainReplacement() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addToBattlefield(player1, new RhoxFaithmender());
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 6);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(2);
    }

    @Test
    void exchangeRecordsLifeGainedThisTurn() {
        addCreatureReady(player1, new EvraHalcyonWitness());
        harness.setLife(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        assertThat(gd.getLifeGainedThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    void exchangeCannotLowerLifeWhenLifeLossIsProhibited() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.setHand(player1, List.of(new EverybodyLives()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(4);
    }

    @Test
    void exchangeDoesNothingIfEvraLeavesBeforeResolution() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(evra);

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void exchangeUsesPowerAndLifeAtResolution() {
        Permanent evra = addCreatureReady(player1, new EvraHalcyonWitness());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        evra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 10);

        harness.passBothPriorities();

        harness.assertLife(player1, 5);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(11);
    }

    @Test
    void tappedSummoningSickEvraCanExchange() {
        Permanent evra = harness.addToBattlefieldAndReturn(player1, new EvraHalcyonWitness());
        evra.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(20);
        assertThat(evra.isTapped()).isTrue();
    }

    @Test
    void exchangedPowerDeterminesCombatDamageAndLifelink() {
        addCreatureReady(player1, new EvraHalcyonWitness());
        harness.setLife(player2, 40);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }
}
