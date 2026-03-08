package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ShrineOfBurningRageTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Shrine of Burning Rage has upkeep trigger, red spell trigger, and activated ability")
    void hasCorrectAbilityStructure() {
        ShrineOfBurningRage card = new ShrineOfBurningRage();

        // Upkeep triggered ability (mandatory charge counter)
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Red spell cast trigger (mandatory charge counter)
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.spellFilter()).isInstanceOf(CardColorPredicate.class);
        assertThat(((CardColorPredicate) trigger.spellFilter()).color()).isEqualTo(CardColor.RED);

        // Activated ability ({3}, {T}, sacrifice: deal damage equal to charge counters to any target)
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToAnyTargetEqualToChargeCountersOnSourceEffect.class);
        assertThat(ability.isNeedsTarget()).isTrue();
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // upkeep trigger goes on stack
        harness.passBothPriorities(); // resolve PutChargeCounterOnSelfEffect

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent shrine = addReadyShrine(player1);

        // First upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(1);

        // Second upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(2);
    }

    // ===== Red spell cast trigger =====

    @Test
    @DisplayName("Casting a red spell puts a charge counter on Shrine")
    void castingRedSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-red spell does not put a charge counter on Shrine")
    void castingNonRedSpellDoesNotAddChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability: deal damage to player =====

    @Test
    @DisplayName("Sacrificing Shrine deals damage equal to charge counters to target player")
    void sacrificeDealsDamageToPlayer() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Sacrificing Shrine with 0 counters deals 0 damage")
    void sacrificeWithZeroCountersDealZeroDamage() {
        Permanent shrine = addReadyShrine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
    }

    // ===== Activated ability: deal damage to creature =====

    @Test
    @DisplayName("Sacrificing Shrine deals damage to target creature")
    void sacrificeDealsDamageToCreature() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, 3 damage should kill it
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
    }

    // ===== Charge counter snapshot survives sacrifice =====

    @Test
    @DisplayName("Charge counters are snapshotted before sacrifice so damage is correct")
    void chargeCountersSnapshotBeforeSacrifice() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(7);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Shrine is sacrificed immediately as cost
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");

        harness.passBothPriorities();

        // Damage should still equal 7 even though Shrine is gone
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    // ===== Activated ability cost enforcement =====

    @Test
    @DisplayName("Activated ability requires tap — cannot activate when tapped")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);
        shrine.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private Permanent addReadyShrine(Player player) {
        ShrineOfBurningRage card = new ShrineOfBurningRage();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
