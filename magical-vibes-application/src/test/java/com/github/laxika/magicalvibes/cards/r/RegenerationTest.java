package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Regeneration.class, GrizzlyBears.class, Forest.class})
class RegenerationTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Regeneration puts it on the stack")
    void castingPutsItOnStack() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving Regeneration attaches it to target creature")
    void resolvesAndAttaches() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Regeneration
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Activated ability grants regeneration shield to enchanted creature")
    void activatedAbilityGrantsShieldToEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regenAura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(regenAura.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activated ability does nothing when Regeneration is not attached")
    void activatedAbilityDoesNothingWhenNotAttached() {
        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(regenAura.getRegenerationShield()).isEqualTo(0);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(log -> log.contains("gains a regeneration shield"));
    }

    @Test
    @DisplayName("Regeneration shield saves enchanted creature from lethal combat damage")
    void regenerationSavesEnchantedCreatureFromLethalCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regenAura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        bears.setBlocking(true);
        bears.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isBlocking()).isFalse();
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(bears.getRegenerationShield()).isZero();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
