package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEqualToSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarwynTheNurturerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has Elf-conditional ON_ALLY_CREATURE_ENTERS_BATTLEFIELD trigger")
    void hasCorrectTrigger() {
        MarwynTheNurturer card = new MarwynTheNurturer();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(SubtypeConditionalEffect.class);

        SubtypeConditionalEffect conditional =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.ELF);
        assertThat(conditional.wrapped()).isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect effect = (PutCountersOnSourceEffect) conditional.wrapped();
        assertThat(effect.powerModifier()).isEqualTo(1);
        assertThat(effect.toughnessModifier()).isEqualTo(1);
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has tap mana ability that produces green mana equal to power")
    void hasCorrectManaAbility() {
        MarwynTheNurturer card = new MarwynTheNurturer();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardManaEqualToSourcePowerEffect.class);

        AwardManaEqualToSourcePowerEffect manaEffect = (AwardManaEqualToSourcePowerEffect) ability.getEffects().getFirst();
        assertThat(manaEffect.color()).isEqualTo(ManaColor.GREEN);
    }

    // ===== Elf trigger =====

    @Test
    @DisplayName("Gets a +1/+1 counter when another Elf enters the battlefield")
    void getsCounterWhenElfEnters() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(marwyn.getPlusOnePlusOneCounters()).isZero();

        // Cast Llanowar Elves (Elf Druid)
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (triggers Marwyn)
        harness.passBothPriorities(); // resolve Marwyn's +1/+1 counter triggered ability

        assertThat(marwyn.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marwyn)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Elf creature enters")
    void noCounterWhenNonElfEnters() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast Grizzly Bears (Bear, not Elf)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(marwyn.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when opponent casts an Elf")
    void noCounterWhenOpponentCastsElf() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Opponent casts an Elf
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(marwyn.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Gets multiple counters from multiple Elf entries")
    void getsMultipleCounters() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast first Elf
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Cast second Elf
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marwyn)).isEqualTo(3);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tap ability produces green mana equal to power (base 1)")
    void tapProducesGreenManaEqualToBasePower() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();
        marwyn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability produces green mana reflecting +1/+1 counters")
    void tapProducesMoreManaWithCounters() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();
        marwyn.setSummoningSick(false);

        // Add two +1/+1 counters (simulating two Elf ETBs)
        marwyn.setPlusOnePlusOneCounters(2);

        harness.activateAbility(player1, 0, 0, null, null);

        // Base power 1 + 2 counters = 3 green mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Integration: Elf ETB gives counter, then tap produces increased mana")
    void elfEtbThenTapProducesCorrectMana() {
        harness.addToBattlefield(player1, new MarwynTheNurturer());

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();
        marwyn.setSummoningSick(false);

        // Cast Llanowar Elves to trigger +1/+1 counter
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(2);

        // Tap Marwyn for mana — should produce 2 green (1 base + 1 counter)
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }
}
