package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExchangeLifeTotalWithCreatureStatEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EvraHalcyonWitnessTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Evra has mana-cost activated ability with ExchangeLifeTotalWithPowerEffect")
    void hasCorrectAbility() {
        EvraHalcyonWitness card = new EvraHalcyonWitness();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{4}");
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(ExchangeLifeTotalWithCreatureStatEffect.class);
    }

    // ===== Exchange behavior =====

    @Test
    @DisplayName("Exchange sets life to power and power to old life total")
    void exchangeLifeAndPower() {
        Permanent evra = addReadyEvra(player1);
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
        Permanent evra = addReadyEvra(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.playerLifeTotals.put(player1.getId(), 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 4, power becomes 2
        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exchange when life equals power does nothing")
    void exchangeWhenLifeEqualsPower() {
        Permanent evra = addReadyEvra(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gd.playerLifeTotals.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple exchanges: second exchange uses updated power")
    void multipleExchanges() {
        Permanent evra = addReadyEvra(player1);
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
        Permanent evra = addReadyEvra(player1);
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
        Permanent evra = addReadyEvra(player1);
        evra.setPlusOnePlusOneCounters(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        // Effective power = 4 + 2 = 6

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Life becomes 6 (effective power including counters)
        // New base power = 20 (old life), + 2 counters = 22
        assertThat(gd.getLife(player1.getId())).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, evra)).isEqualTo(22);
    }

    // ===== Stack behavior =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void putsAbilityOnStack() {
        addReadyEvra(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Evra, Halcyon Witness");
    }

    // ===== Does not require tap =====

    @Test
    @DisplayName("Ability does not tap Evra")
    void abilityDoesNotTap() {
        Permanent evra = addReadyEvra(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(evra.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyEvra(Player player) {
        Permanent perm = new Permanent(new EvraHalcyonWitness());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
