package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegenerationTest extends BaseCardTest {


    @Test
    @DisplayName("Regeneration has correct card properties")
    void hasCorrectProperties() {
        Regeneration card = new Regeneration();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{G}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
    }

    @Test
    @DisplayName("Casting Regeneration puts it on the stack")
    void castingPutsItOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Regeneration");
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving Regeneration attaches it to target creature")
    void resolvesAndAttaches() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Regeneration")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Activated ability grants regeneration shield to enchanted creature")
    void activatedAbilityGrantsShieldToEnchantedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent regenAura = new Permanent(new Regeneration());
        regenAura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(regenAura);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(regenAura.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activated ability does nothing when Regeneration is not attached")
    void activatedAbilityDoesNothingWhenNotAttached() {
        Permanent regenAura = new Permanent(new Regeneration());
        gd.playerBattlefields.get(player1.getId()).add(regenAura);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(regenAura.getRegenerationShield()).isEqualTo(0);
        assertThat(gd.gameLog).noneMatch(log -> log.contains("gains a regeneration shield"));
    }
}
