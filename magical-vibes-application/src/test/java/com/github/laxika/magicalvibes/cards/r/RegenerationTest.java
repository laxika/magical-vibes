package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({Regeneration.class, GiantMantis.class, Mountain.class})
class RegenerationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Regeneration puts it on the stack")
    void castingPutsItOnStack() {
        Permanent creature = addCreatureReady(player1, new GiantMantis());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Resolving Regeneration attaches it to target creature")
    void resolvesAndAttaches() {
        Permanent creature = addCreatureReady(player1, new GiantMantis());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Regeneration
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Activated ability grants regeneration shield to enchanted creature")
    void activatedAbilityGrantsShieldToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GiantMantis());

        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regenAura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(regenAura.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Regeneration shield replaces lethal destruction")
    void shieldReplacesLethalDestruction() {
        Permanent creature = addCreatureReady(player1, new GiantMantis());

        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regenAura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        creature.setMarkedDamage(gqs.getEffectiveToughness(gd, creature));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability regenerates an enchanted creature controlled by an opponent")
    void abilityRegeneratesOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GiantMantis());

        Permanent regenAura = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regenAura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(regenAura.getRegenerationShield()).isZero();
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

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GiantMantis());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Regeneration()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
