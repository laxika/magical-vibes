package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddColorlessManaPerChargeCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShrineOfBoundlessGrowthTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Shrine has upkeep trigger, spell cast trigger, and activated ability")
    void hasCorrectAbilityStructure() {
        ShrineOfBoundlessGrowth card = new ShrineOfBoundlessGrowth();

        // Upkeep triggered ability (mandatory charge counter)
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Spell cast trigger (green spell → charge counter)
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        var spellTrigger = card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(spellTrigger).isInstanceOf(SpellCastTriggerEffect.class);
        SpellCastTriggerEffect trigger = (SpellCastTriggerEffect) spellTrigger;
        assertThat(trigger.spellFilter()).isInstanceOf(CardColorPredicate.class);
        assertThat(((CardColorPredicate) trigger.spellFilter()).color()).isEqualTo(CardColor.GREEN);
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Activated ability (tap + sacrifice → mana)
        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().get(0);
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AddColorlessManaPerChargeCounterOnSourceEffect.class);
    }

    // ===== Upkeep trigger =====

    @Test
    @DisplayName("Upkeep trigger adds a charge counter (mandatory)")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // move to upkeep, trigger fires
        harness.passBothPriorities(); // resolve PutChargeCounterOnSelfEffect

        assertThat(shrine.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeeps accumulate charge counters")
    void multipleUpkeepsAccumulateCounters() {
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

    @Test
    @DisplayName("Opponent's upkeep does not add a charge counter")
    void opponentUpkeepDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Green spell cast trigger =====

    @Test
    @DisplayName("Casting a green spell adds a charge counter")
    void castingGreenSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        GiantGrowth giantGrowth = new GiantGrowth();
        harness.setHand(player1, List.of(giantGrowth));
        // Giant Growth targets a creature, so we need a creature
        Card bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.castInstant(player1, 0, creature.getId());

        // Spell cast trigger should put charge counter on shrine
        harness.passBothPriorities(); // resolve charge counter trigger
        assertThat(shrine.getChargeCounters()).isEqualTo(1);

        // Resolve the Giant Growth itself
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a non-green spell does not add a charge counter")
    void castingNonGreenSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        // Shock targets any target — target opponent
        harness.castInstant(player1, 0, player2.getId());

        // No charge counter trigger should fire — resolve Shock
        harness.passBothPriorities();

        assertThat(shrine.getChargeCounters()).isEqualTo(0);
    }

    // ===== Activated ability: Tap + sacrifice for mana =====

    @Test
    @DisplayName("Sacrificing with charge counters adds colorless mana")
    void sacrificeAddsColorlessManaPerChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(4);

        int colorlessBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        // Mana ability resolves immediately (no stack)
        int colorlessAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        assertThat(colorlessAfter - colorlessBefore).isEqualTo(4);

        // Shrine should be in graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Boundless Growth"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shrine of Boundless Growth"));
    }

    @Test
    @DisplayName("Sacrificing with zero counters adds no mana")
    void sacrificeWithZeroCountersAddsNoMana() {
        Permanent shrine = addReadyShrine(player1);
        // No charge counters

        int colorlessBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);

        harness.activateAbility(player1, 0, null, null);

        int colorlessAfter = gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS);
        assertThat(colorlessAfter - colorlessBefore).isEqualTo(0);

        // Shrine should still be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Shrine of Boundless Growth"));
    }

    @Test
    @DisplayName("Activated ability requires tap — tapped shrine cannot activate")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana ability resolves immediately without using the stack")
    void manaAbilityResolvesImmediately() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setChargeCounters(3);

        int stackSizeBefore = gd.stack.size();

        harness.activateAbility(player1, 0, null, null);

        // Should not add anything to the stack (mana ability)
        assertThat(gd.stack.size()).isEqualTo(stackSizeBefore);

        // Mana should already be in pool
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(3);
    }

    // ===== Helper methods =====

    private Permanent addReadyShrine(Player player) {
        ShrineOfBoundlessGrowth card = new ShrineOfBoundlessGrowth();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
