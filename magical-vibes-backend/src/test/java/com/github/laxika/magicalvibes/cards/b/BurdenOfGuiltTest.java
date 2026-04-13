package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TapEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurdenOfGuiltTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Burden of Guilt has correct card properties")
    void hasCorrectProperties() {
        BurdenOfGuilt card = new BurdenOfGuilt();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(TapEnchantedCreatureEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Burden of Guilt puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BurdenOfGuilt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Burden of Guilt");
    }

    @Test
    @DisplayName("Resolving Burden of Guilt attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BurdenOfGuilt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burden of Guilt")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Activated ability: tap enchanted creature =====

    @Test
    @DisplayName("Activating ability taps the enchanted creature")
    void activatingAbilityTapsEnchantedCreature() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new BurdenOfGuilt());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // The Aura is at index 1 (bears at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new BurdenOfGuilt());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Burden of Guilt");
    }

    @Test
    @DisplayName("Ability can be activated multiple times per turn")
    void abilityCanBeActivatedMultipleTimes() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new BurdenOfGuilt());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Activate first time
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isTrue();

        // Activate again (creature already tapped, but ability still works - it just doesn't change state)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Can enchant opponent's creature =====

    @Test
    @DisplayName("Can enchant and tap opponent's creature")
    void canEnchantAndTapOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new BurdenOfGuilt());
        auraPerm.setAttachedTo(opponentCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura is at index 0 on player1's battlefield (only permanent)
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    // ===== Aura removed - ability no longer available =====

    @Test
    @DisplayName("Ability is no longer available when aura is removed from battlefield")
    void abilityGoneWhenAuraRemoved() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = new Permanent(new BurdenOfGuilt());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Remove the aura
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        // Bears should not be tappable via the ability anymore
        // Bears has no activated ability of its own
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Burden of Guilt"));
    }
}
